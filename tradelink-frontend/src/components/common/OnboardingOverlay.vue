<template>
  <Teleport to="body">
    <Transition name="onboarding-fade">
      <div v-if="active && step" class="onboarding-overlay" @click.self="skip">
        <!-- Spotlight mask (SVG with transparent hole) -->
        <svg class="onboarding-mask" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <mask id="onboarding-hole">
              <rect width="100%" height="100%" fill="white" />
              <rect
                :x="spotRect.x - 8"
                :y="spotRect.y - 8"
                :width="spotRect.w + 16"
                :height="spotRect.h + 16"
                rx="12"
                fill="black"
              />
            </mask>
          </defs>
          <rect
            width="100%"
            height="100%"
            fill="rgba(15, 23, 42, 0.65)"
            mask="url(#onboarding-hole)"
          />
        </svg>

        <!-- Spotlight ring -->
        <div
          class="onboarding-ring"
          :style="{
            top: (spotRect.y - 8) + 'px',
            left: (spotRect.x - 8) + 'px',
            width: (spotRect.w + 16) + 'px',
            height: (spotRect.h + 16) + 'px'
          }"
        ></div>

        <!-- Tooltip -->
        <div class="onboarding-tooltip" :style="tooltipStyle" :class="'onboarding-tooltip--' + tooltipPosition">
          <div class="onboarding-tooltip__header">
            <h4 class="onboarding-tooltip__title">{{ step.title }}</h4>
            <button type="button" class="onboarding-tooltip__close" @click="skip" aria-label="Fechar tour">
              &times;
            </button>
          </div>
          <p class="onboarding-tooltip__message">{{ step.message }}</p>
          <div class="onboarding-tooltip__footer">
            <span class="onboarding-tooltip__counter">{{ currentStep + 1 }} de {{ totalSteps }}</span>
            <div class="onboarding-tooltip__actions">
              <button v-if="!isFirst" type="button" class="onboarding-btn onboarding-btn--ghost" @click="prev">Anterior</button>
              <button type="button" class="onboarding-btn onboarding-btn--ghost" @click="skip">Pular</button>
              <button type="button" class="onboarding-btn onboarding-btn--primary" @click="next">
                {{ isLast ? 'Concluir' : 'Próximo' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'

const props = defineProps({
  active: { type: Boolean, default: false },
  step: { type: Object, default: null },
  currentStep: { type: Number, default: 0 },
  totalSteps: { type: Number, default: 0 },
  isFirst: { type: Boolean, default: true },
  isLast: { type: Boolean, default: false }
})

const emit = defineEmits(['next', 'prev', 'skip'])

const TOOLTIP_GAP = 16

const spotRect = ref({ x: 0, y: 0, w: 0, h: 0 })

const isMobile = computed(() => window.innerWidth < 640)

const tooltipWidth = computed(() => isMobile.value ? Math.min(300, window.innerWidth - 24) : 340)

const tooltipPosition = computed(() => {
  const preferred = props.step?.position || 'bottom'
  // On mobile, force bottom unless it's a sidebar target (which needs right)
  if (isMobile.value) {
    const isSidebarTarget = props.step?.target?.includes('data-sidebar-link')
    if (isSidebarTarget) return 'bottom'
    if (preferred === 'left' || preferred === 'right') return 'bottom'
  }
  return preferred
})

const tooltipStyle = computed(() => {
  const r = spotRect.value
  const pos = tooltipPosition.value
  const tw = tooltipWidth.value
  const style = { width: tw + 'px' }

  if (pos === 'bottom') {
    style.top = (r.y + r.h + TOOLTIP_GAP) + 'px'
    style.left = Math.max(12, Math.min(r.x + r.w / 2 - tw / 2, window.innerWidth - tw - 12)) + 'px'
  } else if (pos === 'top') {
    style.top = (r.y - TOOLTIP_GAP) + 'px'
    style.left = Math.max(12, Math.min(r.x + r.w / 2 - tw / 2, window.innerWidth - tw - 12)) + 'px'
    style.transform = 'translateY(-100%)'
  } else if (pos === 'right') {
    style.top = (r.y + r.h / 2) + 'px'
    style.left = (r.x + r.w + TOOLTIP_GAP) + 'px'
    style.transform = 'translateY(-50%)'
  } else if (pos === 'left') {
    style.top = (r.y + r.h / 2) + 'px'
    style.left = (r.x - TOOLTIP_GAP - tw) + 'px'
    style.transform = 'translateY(-50%)'
  }

  return style
})

function updateSpot() {
  if (!props.step?.target) {
    spotRect.value = { x: window.innerWidth / 2 - 50, y: window.innerHeight / 2 - 25, w: 100, h: 50 }
    return
  }

  // If targeting sidebar link on mobile, open sidebar first
  const isSidebarTarget = props.step.target.includes('data-sidebar-link')
  if (isSidebarTarget && window.innerWidth < 1024) {
    const sidebar = document.querySelector('.sidebar')
    if (sidebar && !sidebar.classList.contains('sidebar--open')) {
      sidebar.classList.add('sidebar--open')
      const backdrop = document.querySelector('.sidebar__backdrop')
      if (backdrop) backdrop.style.display = 'block'
    }
    // Wait for sidebar animation to complete
    setTimeout(() => positionSpot(), 300)
    return
  }

  positionSpot()
}

function positionSpot() {
  const el = document.querySelector(props.step.target)
  if (!el) {
    spotRect.value = { x: window.innerWidth / 2 - 50, y: window.innerHeight / 2 - 25, w: 100, h: 50 }
    return
  }
  const rect = el.getBoundingClientRect()
  spotRect.value = {
    x: rect.left + window.scrollX,
    y: rect.top + window.scrollY,
    w: rect.width,
    h: rect.height
  }
  // Scroll element into view if needed
  el.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

function closeSidebarIfOpen() {
  if (window.innerWidth < 1024) {
    const sidebar = document.querySelector('.sidebar')
    if (sidebar && sidebar.classList.contains('sidebar--open')) {
      sidebar.classList.remove('sidebar--open')
    }
  }
}

function next() { emit('next') }
function prev() { emit('prev') }
function skip() { closeSidebarIfOpen(); emit('skip') }

// Re-calculate when step changes
watch(() => [props.active, props.currentStep], async () => {
  if (props.active && props.step) {
    // Close sidebar if new step doesn't target sidebar
    const isSidebarTarget = props.step.target?.includes('data-sidebar-link')
    if (!isSidebarTarget) closeSidebarIfOpen()
    await nextTick()
    setTimeout(updateSpot, 100)
  } else if (!props.active) {
    closeSidebarIfOpen()
  }
}, { immediate: true })

// Recalc on resize
function onResize() {
  if (props.active) updateSpot()
}

onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))
</script>

<style scoped>
.onboarding-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
}

.onboarding-mask {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.onboarding-ring {
  position: absolute;
  border: 2px solid rgba(99, 102, 241, 0.7);
  border-radius: 12px;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.2);
  pointer-events: none;
  transition: all 0.35s ease;
  animation: onboarding-pulse 2s infinite;
}

@keyframes onboarding-pulse {
  0%, 100% { box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.2); }
  50% { box-shadow: 0 0 0 8px rgba(99, 102, 241, 0.12); }
}

.onboarding-tooltip {
  position: absolute;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 40px -8px rgba(15, 23, 42, 0.25), 0 0 0 1px rgba(99, 102, 241, 0.15);
  padding: 1.25rem;
  z-index: 10000;
  transition: top 0.3s ease, left 0.3s ease;
}

.onboarding-tooltip__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.onboarding-tooltip__title {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  color: #1e293b;
}

.onboarding-tooltip__close {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: #94a3b8;
  cursor: pointer;
  line-height: 1;
  padding: 0 2px;
}
.onboarding-tooltip__close:hover {
  color: #475569;
}

.onboarding-tooltip__message {
  margin: 0 0 1rem;
  font-size: 0.875rem;
  color: #475569;
  line-height: 1.5;
}

.onboarding-tooltip__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.onboarding-tooltip__counter {
  font-size: 0.75rem;
  color: #94a3b8;
  font-weight: 500;
}

.onboarding-tooltip__actions {
  display: flex;
  gap: 0.5rem;
}

.onboarding-btn {
  padding: 0.375rem 0.875rem;
  border-radius: 8px;
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  border: none;
  font-family: inherit;
}

.onboarding-btn--primary {
  background: #6366f1;
  color: #fff;
}
.onboarding-btn--primary:hover {
  background: #4f46e5;
}

.onboarding-btn--ghost {
  background: transparent;
  color: #6366f1;
}
.onboarding-btn--ghost:hover {
  background: #eef2ff;
}

/* Mobile responsiveness */
@media (max-width: 639px) {
  .onboarding-tooltip {
    max-width: calc(100vw - 24px);
    padding: 1rem;
  }

  .onboarding-tooltip__title {
    font-size: 0.9375rem;
  }

  .onboarding-tooltip__message {
    font-size: 0.8125rem;
  }

  .onboarding-tooltip__footer {
    flex-direction: column;
    gap: 0.5rem;
    align-items: stretch;
  }

  .onboarding-tooltip__counter {
    text-align: center;
  }

  .onboarding-tooltip__actions {
    justify-content: center;
  }

  .onboarding-btn {
    padding: 0.5rem 0.75rem;
    font-size: 0.8125rem;
  }
}

/* Fade transition */
.onboarding-fade-enter-active,
.onboarding-fade-leave-active {
  transition: opacity 0.3s ease;
}
.onboarding-fade-enter-from,
.onboarding-fade-leave-to {
  opacity: 0;
}
</style>
