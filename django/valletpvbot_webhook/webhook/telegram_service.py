"""
Servicio para interactuar con la API de Telegram.
"""

import logging
import requests
from django.conf import settings
from typing import Optional, Dict, Any

logger = logging.getLogger(__name__)


class TelegramService:
    """
    Servicio para enviar mensajes a través del bot de Telegram.
    """
    
    def __init__(self):
        self.token = settings.TELEGRAM_BOT_TOKEN
        self.base_url = f"https://api.telegram.org/bot{self.token}"
    
    def send_message(
        self, 
        chat_id: int, 
        text: str, 
        parse_mode: str = 'HTML',
        reply_markup: Optional[Dict] = None
    ) -> Optional[Dict[str, Any]]:
        """
        Envía un mensaje de texto a un chat.
        
        Args:
            chat_id: ID del chat de Telegram
            text: Texto del mensaje
            parse_mode: Modo de parseo (HTML, Markdown)
            reply_markup: Teclado personalizado
            
        Returns:
            Respuesta de la API de Telegram o None si falla
        """
        url = f"{self.base_url}/sendMessage"
        payload = {
            'chat_id': chat_id,
            'text': text,
            'parse_mode': parse_mode
        }
        
        if reply_markup:
            payload['reply_markup'] = reply_markup
        
        try:
            response = requests.post(url, json=payload, timeout=10)
            response.raise_for_status()
            result = response.json()
            
            if result.get('ok'):
                logger.info(f"Mensaje enviado a {chat_id}")
                return result.get('result')
            else:
                logger.error(f"Error al enviar mensaje: {result.get('description')}")
                return None
                
        except requests.exceptions.RequestException as e:
            logger.error(f"Error de conexión al enviar mensaje: {str(e)}")
            return None
    
    def send_photo(
        self, 
        chat_id: int, 
        photo_url: str, 
        caption: str = '',
        parse_mode: str = 'HTML'
    ) -> Optional[Dict[str, Any]]:
        """
        Envía una foto a un chat.
        
        Args:
            chat_id: ID del chat de Telegram
            photo_url: URL de la foto
            caption: Texto de descripción
            parse_mode: Modo de parseo
            
        Returns:
            Respuesta de la API de Telegram o None si falla
        """
        url = f"{self.base_url}/sendPhoto"
        payload = {
            'chat_id': chat_id,
            'photo': photo_url,
            'caption': caption,
            'parse_mode': parse_mode
        }
        
        try:
            response = requests.post(url, json=payload, timeout=10)
            response.raise_for_status()
            result = response.json()
            
            if result.get('ok'):
                logger.info(f"Foto enviada a {chat_id}")
                return result.get('result')
            else:
                logger.error(f"Error al enviar foto: {result.get('description')}")
                return None
                
        except requests.exceptions.RequestException as e:
            logger.error(f"Error de conexión al enviar foto: {str(e)}")
            return None
    
    def send_document(
        self, 
        chat_id: int, 
        document_url: str, 
        caption: str = ''
    ) -> Optional[Dict[str, Any]]:
        """
        Envía un documento a un chat.
        
        Args:
            chat_id: ID del chat de Telegram
            document_url: URL del documento
            caption: Texto de descripción
            
        Returns:
            Respuesta de la API de Telegram o None si falla
        """
        url = f"{self.base_url}/sendDocument"
        payload = {
            'chat_id': chat_id,
            'document': document_url,
            'caption': caption
        }
        
        try:
            response = requests.post(url, json=payload, timeout=10)
            response.raise_for_status()
            result = response.json()
            
            if result.get('ok'):
                logger.info(f"Documento enviado a {chat_id}")
                return result.get('result')
            else:
                logger.error(f"Error al enviar documento: {result.get('description')}")
                return None
                
        except requests.exceptions.RequestException as e:
            logger.error(f"Error de conexión al enviar documento: {str(e)}")
            return None
    
    def set_webhook(self, webhook_url: str) -> bool:
        """
        Configura el webhook del bot de Telegram.
        
        Args:
            webhook_url: URL del webhook
            
        Returns:
            True si se configuró correctamente, False en caso contrario
        """
        url = f"{self.base_url}/setWebhook"
        payload = {
            'url': webhook_url,
            'allowed_updates': ['message', 'callback_query']
        }
        
        try:
            response = requests.post(url, json=payload, timeout=10)
            response.raise_for_status()
            result = response.json()
            
            if result.get('ok'):
                logger.info(f"Webhook configurado: {webhook_url}")
                return True
            else:
                logger.error(f"Error al configurar webhook: {result.get('description')}")
                return False
                
        except requests.exceptions.RequestException as e:
            logger.error(f"Error de conexión al configurar webhook: {str(e)}")
            return False
    
    def get_webhook_info(self) -> Optional[Dict[str, Any]]:
        """
        Obtiene información del webhook configurado.
        
        Returns:
            Información del webhook o None si falla
        """
        url = f"{self.base_url}/getWebhookInfo"
        
        try:
            response = requests.get(url, timeout=10)
            response.raise_for_status()
            result = response.json()
            
            if result.get('ok'):
                return result.get('result')
            else:
                logger.error(f"Error al obtener info del webhook: {result.get('description')}")
                return None
                
        except requests.exceptions.RequestException as e:
            logger.error(f"Error de conexión al obtener info del webhook: {str(e)}")
            return None
    
    def delete_message(self, chat_id: int, message_id: int) -> bool:
        """
        Borra un mensaje de Telegram.
        
        Args:
            chat_id: ID del chat de Telegram
            message_id: ID del mensaje a borrar
            
        Returns:
            True si se borró correctamente, False en caso contrario
        """
        url = f"{self.base_url}/deleteMessage"
        payload = {
            'chat_id': chat_id,
            'message_id': message_id
        }
        
        try:
            response = requests.post(url, json=payload, timeout=10)
            response.raise_for_status()
            result = response.json()
            
            if result.get('ok'):
                logger.info(f"Mensaje {message_id} borrado del chat {chat_id}")
                return True
            else:
                logger.error(f"Error al borrar mensaje: {result.get('description')}")
                return False
                
        except requests.exceptions.RequestException as e:
            logger.error(f"Error de conexión al borrar mensaje: {str(e)}")
            return False
    
    def edit_message(
        self, 
        chat_id: int, 
        message_id: int,
        text: str,
        parse_mode: str = 'HTML',
        reply_markup: Optional[Dict] = None
    ) -> bool:
        """
        Edita un mensaje existente de Telegram.
        
        Args:
            chat_id: ID del chat de Telegram
            message_id: ID del mensaje a editar
            text: Nuevo texto del mensaje
            parse_mode: Modo de parseo (HTML, Markdown)
            reply_markup: Teclado personalizado (None para quitar botones)
            
        Returns:
            True si se editó correctamente, False en caso contrario
        """
        url = f"{self.base_url}/editMessageText"
        payload = {
            'chat_id': chat_id,
            'message_id': message_id,
            'text': text,
            'parse_mode': parse_mode
        }
        
        if reply_markup is not None:
            payload['reply_markup'] = reply_markup
        
        try:
            response = requests.post(url, json=payload, timeout=10)
            response.raise_for_status()
            result = response.json()
            
            if result.get('ok'):
                logger.info(f"Mensaje {message_id} editado en chat {chat_id}")
                return True
            else:
                logger.error(f"Error al editar mensaje: {result.get('description')}")
                return False
                
        except requests.exceptions.RequestException as e:
            logger.error(f"Error de conexión al editar mensaje: {str(e)}")
            return False
