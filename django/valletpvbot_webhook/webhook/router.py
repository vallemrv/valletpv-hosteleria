"""
Servicio de routing para determinar a qué TPV corresponde cada usuario.
Implementa estrategias de balanceo de carga y asignación de usuarios.
"""

from typing import Optional
from django.db.models import Count, Q
from .models import TPVInstance, TelegramUser
import logging

logger = logging.getLogger(__name__)


class TPVRouter:
    """
    Router que asigna usuarios de Telegram a instancias TPV.
    Soporta diferentes estrategias de routing.
    """
    
    STRATEGY_ROUND_ROBIN = 'round_robin'
    STRATEGY_LEAST_USERS = 'least_users'
    STRATEGY_SPECIFIC = 'specific'
    
    def __init__(self, strategy: str = STRATEGY_LEAST_USERS):
        self.strategy = strategy
    
    def get_or_assign_tpv(self, telegram_user: TelegramUser) -> Optional[TPVInstance]:
        """
        Obtiene el TPV asignado al usuario o le asigna uno nuevo.
        
        Args:
            telegram_user: Usuario de Telegram
            
        Returns:
            Instancia TPV asignada o None si no hay TPVs disponibles
        """
        # Si el usuario ya tiene TPV asignado, devolverlo
        if telegram_user.tpv_instance and telegram_user.tpv_instance.is_active:
            return telegram_user.tpv_instance
        
        # Asignar un nuevo TPV según la estrategia
        tpv = self._select_tpv()
        
        if tpv:
            telegram_user.tpv_instance = tpv
            telegram_user.save(update_fields=['tpv_instance'])
            logger.info(f"Usuario {telegram_user.telegram_id} asignado a TPV {tpv.name}")
        else:
            logger.warning(f"No se pudo asignar TPV al usuario {telegram_user.telegram_id}")
        
        return tpv
    
    def _select_tpv(self) -> Optional[TPVInstance]:
        """
        Selecciona un TPV según la estrategia configurada.
        
        Returns:
            Instancia TPV seleccionada o None
        """
        if self.strategy == self.STRATEGY_LEAST_USERS:
            return self._select_least_users()
        elif self.strategy == self.STRATEGY_ROUND_ROBIN:
            return self._select_round_robin()
        else:
            return self._select_least_users()
    
    def _select_least_users(self) -> Optional[TPVInstance]:
        """
        Selecciona el TPV con menos usuarios asignados que no haya alcanzado su límite.
        """
        tpv = TPVInstance.objects.filter(
            is_active=True
        ).annotate(
            user_count=Count('users', filter=Q(users__is_active=True))
        ).filter(
            user_count__lt=models.F('max_users')
        ).order_by('user_count').first()
        
        return tpv
    
    def _select_round_robin(self) -> Optional[TPVInstance]:
        """
        Selecciona TPVs de forma rotativa.
        """
        # Obtener TPVs activos que no hayan alcanzado su límite
        tpvs = TPVInstance.objects.filter(
            is_active=True
        ).annotate(
            user_count=Count('users', filter=Q(users__is_active=True))
        ).filter(
            user_count__lt=models.F('max_users')
        ).order_by('last_heartbeat')
        
        return tpvs.first() if tpvs.exists() else None
    
    def reassign_users_from_tpv(self, failed_tpv: TPVInstance) -> int:
        """
        Reasigna usuarios de un TPV que ha fallado a otros TPVs disponibles.
        
        Args:
            failed_tpv: TPV que ha fallado
            
        Returns:
            Número de usuarios reasignados
        """
        users = TelegramUser.objects.filter(
            tpv_instance=failed_tpv,
            is_active=True
        )
        
        reassigned_count = 0
        
        for user in users:
            user.tpv_instance = None
            new_tpv = self._select_tpv()
            
            if new_tpv:
                user.tpv_instance = new_tpv
                user.save(update_fields=['tpv_instance'])
                reassigned_count += 1
                logger.info(f"Usuario {user.telegram_id} reasignado de {failed_tpv.name} a {new_tpv.name}")
        
        return reassigned_count
    
    def get_tpv_stats(self):
        """
        Obtiene estadísticas de todos los TPVs.
        
        Returns:
            Lista de diccionarios con estadísticas de cada TPV
        """
        tpvs = TPVInstance.objects.annotate(
            user_count=Count('users', filter=Q(users__is_active=True)),
            pending_instructions=Count('instructions', filter=Q(instructions__status='pending'))
        ).values(
            'id', 'name', 'is_active', 'user_count', 'pending_instructions', 
            'max_users', 'last_heartbeat'
        )
        
        return list(tpvs)


# Importar models aquí para evitar circular imports
from django.db import models
