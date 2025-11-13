# chatbot/models.py

from django.db import models
from django.contrib.auth.models import User


class Category(models.Model):
    """
    Categorías personalizables por usuario
    """
    name = models.CharField(max_length=50, verbose_name="Nombre de la categoría")
    user = models.ForeignKey(
        User, 
        on_delete=models.CASCADE, 
        verbose_name="Usuario"
    )
    created_at = models.DateTimeField(auto_now_add=True, verbose_name="Fecha de creación")
    
    class Meta:
        db_table = 'chatbot_categories'
        verbose_name = 'Categoría'
        verbose_name_plural = 'Categorías'
        ordering = ['name']
        unique_together = ['name', 'user']  # No duplicar nombres por usuario
    
    def __str__(self):
        return f"{self.name} ({self.user.username})"


class SavedMessage(models.Model):
    """
    Modelo simple para guardar mensajes del chatbot por usuario
    """
    # Campos básicos
    titulo = models.CharField(max_length=200, null=True, blank=True, verbose_name="Título")
    texto_html_raw = models.TextField(verbose_name="Texto HTML del mensaje")
    category = models.ForeignKey(
        Category,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        verbose_name="Categoría"
    )
    user = models.ForeignKey(
        User, 
        on_delete=models.CASCADE, 
        verbose_name="Usuario"
    )
    
    # Metadatos básicos
    created_at = models.DateTimeField(auto_now_add=True, verbose_name="Fecha de creación")
    
    class Meta:
        db_table = 'chatbot_saved_messages'
        verbose_name = 'Mensaje Guardado del Chatbot'
        verbose_name_plural = 'Mensajes Guardados del Chatbot'
        ordering = ['-created_at']

    def __str__(self):
        if self.titulo:
            category_name = self.category.name if self.category else 'Sin categoría'
            return f"[{category_name}] {self.titulo}"
        else:
            # Mostramos los primeros 50 caracteres del texto sin HTML
            texto_limpio = self.texto_html_raw[:50].strip()
            category_name = self.category.name if self.category else 'Sin categoría'
            return f"[{category_name}] {texto_limpio}..."


