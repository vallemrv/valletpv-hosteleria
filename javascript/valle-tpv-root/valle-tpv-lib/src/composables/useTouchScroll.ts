import { onMounted, onUnmounted, type Ref } from 'vue';

/**
 * Composable para habilitar scroll táctil en elementos
 * Útil para Ubuntu/Linux con Xorg donde el scroll táctil nativo no funciona bien
 */
export function useTouchScroll(elementRef: Ref<HTMLElement | null>) {
  let startY = 0;
  let startX = 0;
  let scrollTop = 0;
  let scrollLeft = 0;
  let isScrolling = false;
  let velocityY = 0;
  let velocityX = 0;
  let lastY = 0;
  let lastX = 0;
  let lastTime = 0;
  let momentumId: number | null = null;

  const handleTouchStart = (e: TouchEvent) => {
    if (!elementRef.value) return;
    
    const touch = e.touches[0];
    startY = touch.pageY;
    startX = touch.pageX;
    scrollTop = elementRef.value.scrollTop;
    scrollLeft = elementRef.value.scrollLeft;
    isScrolling = true;
    
    lastY = touch.pageY;
    lastX = touch.pageX;
    lastTime = Date.now();
    velocityY = 0;
    velocityX = 0;
    
    // Cancelar momentum si existe
    if (momentumId !== null) {
      cancelAnimationFrame(momentumId);
      momentumId = null;
    }
  };

  const handleTouchMove = (e: TouchEvent) => {
    if (!isScrolling || !elementRef.value) return;
    
    const touch = e.touches[0];
    const currentY = touch.pageY;
    const currentX = touch.pageX;
    const currentTime = Date.now();
    
    const deltaY = startY - currentY;
    const deltaX = startX - currentX;
    
    // Calcular velocidad para el efecto de inercia
    const timeDiff = currentTime - lastTime;
    if (timeDiff > 0) {
      velocityY = (currentY - lastY) / timeDiff;
      velocityX = (currentX - lastX) / timeDiff;
    }
    
    lastY = currentY;
    lastX = currentX;
    lastTime = currentTime;
    
    // Aplicar scroll
    elementRef.value.scrollTop = scrollTop + deltaY;
    elementRef.value.scrollLeft = scrollLeft + deltaX;
    
    // Prevenir scroll del body
    e.preventDefault();
  };

  const handleTouchEnd = () => {
    if (!isScrolling || !elementRef.value) return;
    
    isScrolling = false;
    
    // Aplicar efecto de inercia (momentum scrolling)
    const friction = 0.95;
    const minVelocity = 0.1;
    
    const applyMomentum = () => {
      if (!elementRef.value) return;
      
      // Reducir velocidad con fricción
      velocityY *= friction;
      velocityX *= friction;
      
      // Detener si la velocidad es muy baja
      if (Math.abs(velocityY) < minVelocity && Math.abs(velocityX) < minVelocity) {
        momentumId = null;
        return;
      }
      
      // Aplicar scroll con inercia
      elementRef.value.scrollTop -= velocityY * 10;
      elementRef.value.scrollLeft -= velocityX * 10;
      
      // Continuar animación
      momentumId = requestAnimationFrame(applyMomentum);
    };
    
    // Iniciar momentum solo si hay velocidad significativa
    if (Math.abs(velocityY) > minVelocity || Math.abs(velocityX) > minVelocity) {
      applyMomentum();
    }
  };

  onMounted(() => {
    const element = elementRef.value;
    if (!element) {
      console.warn('useTouchScroll: elemento no encontrado');
      return;
    }
    
    // Verificar que es un elemento HTML válido
    if (!(element instanceof HTMLElement)) {
      console.warn('useTouchScroll: el ref no apunta a un HTMLElement válido', element);
      return;
    }
    
    // Agregar event listeners con passive: false para poder usar preventDefault
    element.addEventListener('touchstart', handleTouchStart, { passive: false });
    element.addEventListener('touchmove', handleTouchMove, { passive: false });
    element.addEventListener('touchend', handleTouchEnd, { passive: true });
    element.addEventListener('touchcancel', handleTouchEnd, { passive: true });
  });

  onUnmounted(() => {
    const element = elementRef.value;
    if (!element || !(element instanceof HTMLElement)) return;
    
    element.removeEventListener('touchstart', handleTouchStart);
    element.removeEventListener('touchmove', handleTouchMove);
    element.removeEventListener('touchend', handleTouchEnd);
    element.removeEventListener('touchcancel', handleTouchEnd);
    
    // Cancelar momentum si existe
    if (momentumId !== null) {
      cancelAnimationFrame(momentumId);
    }
  });

  return {
    // Podríamos exponer métodos adicionales aquí si es necesario
  };
}
