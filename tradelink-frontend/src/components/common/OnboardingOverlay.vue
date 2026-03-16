<template>
  <Teleport to="body">
    <Transition name="onboarding-fade">
      <div v-if="active && step" class="onboarding-overlay" @click.self="skip">
        <!-- Dark backdrop with cutout hole -->
        <svg class="onboarding-mask" width="100%" height="100%">
          <defs>
            <mask id="onboarding-spotlight-mask">
              <rect width="100%" height="100%" fill="white" />
              <rect
                :x="spot.x - 10"
                :y="spot.y - 10"
                :width="spot.w + 20"
                :height="spot.h + 20"
                rx="14"
                fill="black"
              />
            </mask>
          </defs>
          <rect
            width="100%"
            height="100%"
            fill="rgba(15, 23, 42, 0.7)"
            mask="url(#onboarding-spotlight-mask)"
          />
        </svg>

        <!-- Pulsing ring around the element -->
        <div
          class="onboarding-ring"
          :style="{
            top: (spot.y - 10) + 'px',
            left: (spot.x - 10) + 'px',
            width: (spot.w + 20) + 'px',
            height: (spot.h + 20) + 'px'
          }"
        />

        <!-- Tooltip card -->
        <div
          ref="tooltipRef"
          class="onboarding-tooltip"
          :style="tooltipStyle"
        >
          <!-- Step icon + counter -->
          <div class="onboarding-tooltip__top">
            <div class="onboarding-tooltip__step-badge">
              <span class="onboarding-tooltip__step-num">{{ currentStep + 1 }}</span>
              <span class="onboarding-tooltip__step-total">/ {{ totalSteps }}</span>
            </div>
            <button type="button" class="onboarding-tooltip__close" @click="skip" aria-label="Fechar tutorial">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>

          <!-- Title -->
          <h4 class="onboarding-tooltip__title">{{ step.title }}</h4>

          <!-- Message -->
          <p class="onboarding-tooltip__message">{{ step.message }}</p>

          <!-- Progress bar -->
          <div class="onboarding-tooltip__progress">
            <div class="onboarding-tooltip__progress-bar" :style="{ width: ((currentStep + 1) / totalSteps * 100) + '%' }"></div>
          </div>

          <!-- Footer actions -->
          <div class="onboarding-tooltip__footer">
            <button v-if="!isFirst" type="button" class="onboarding-btn onboarding-btn--ghost" @click="prev">
              ← Anterior
            </button>
            <span v-else></span>
            <div class="onboarding-tooltip__footer-right">
              <button type="button" class="onboarding-btn onboarding-btn--ghost onboarding-btn--skip" @click="skip">
                Pular tutorial
              </button>
              <button type="button" class="onboarding-btn onboarding-btn--primary" @click="next">
                {{ isLast ? '✓ Concluir' : 'Próximo →' }}
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

const tooltipRef = ref(null)

const GAP = 20

// Viewport-relative spotlight rect (getBoundingClientRect values)
const spot = ref({ x: 0, y: 0, w: 0, h: 0 })

const isMobile = ref(window.innerWidth < 768)

const tooltipStyle = computed(() => {
  const s = spot.value
  const style = {}
  const tw = isMobile.value ? Math.min(380, window.innerWidth - 32) : 420
  style.width = tw + 'px'
  style.maxWidth = 'calc(100vw - 32px)'

  if (isMobile.value) {
    // On mobile: always center at bottom of screen
    style.bottom = '16px'
    style.left = '50%'
    style.transform = 'translateX(-50%)'
    style.top = 'auto'
    return style
  }

  // Desktop: position relative to spotlight
  const viewH = window.innerHeight
  const viewW = window.innerWidth
  const spotBottom = s.y + s.h
  const spotCenterX = s.x + s.w / 2
  const spaceBelow = viewH - spotBottom
  const spaceAbove = s.y

  // Prefer below, then above, then center
  if (spaceBelow > 280) {
    style.top = (spotBottom + GAP) + 'px'
  } else if (spaceAbove > 280) {
    style.bottom = (viewH - s.y + GAP) + 'px'
  } else {
    // Center vertically
    style.top = '50%'
    style.transform = 'translateY(-50%)'
  }

  // Horizontal: try to center on element, clamp to viewport
  let leftPos = spotCenterX - tw / 2
  leftPos = Math.max(16, Math.min(leftPos, viewW - tw - 16))
  style.left = leftPos + 'px'

  return style
})

function updateSpot() {
  if (!props.step?.target) {
    // No target - center a virtual spot
    spot.value = { x: window.innerWidth / 2 - 60, y: window.innerHeight / 2 - 30, w: 120, h: 60 }
    return
  }

  // Open sidebar if needed for sidebar targets
  const isSidebarTarget = props.step.target.includes('data-sidebar-link')
  if (isSidebarTarget && window.innerWidth < 1024) {
    const sidebar = document.querySelector('.sidebar')
    if (sidebar && !sidebar.classList.contains('sidebar--open')) {
      sidebar.classList.add('sidebar--open')
    }
    setTimeout(() => positionSpot(), 350)
    return
  }

  positionSpot()
}

function positionSpot() {
  const el = document.querySelector(props.step?.target)
  if (!el) {
    spot.value = { x: window.innerWidth / 2 - 60, y: window.innerHeight / 2 - 30, w: 120, h: 60 }
    return
  }

  // Scroll element into view first
  el.scrollIntoView({ behavior: 'smooth', block: 'center' })

  // Use a small delay to let scroll finish, then get final position
  setTimeout(() => {
    const rect = el.getBoundingClientRect()
    spot.value = {
      x: rect.left,
      y: rect.top,
      w: rect.width,
      h: rect.height
    }
  }, 300)
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

// Watch step changes
watch(() => [props.active, props.currentStep], async () => {
  if (props.active && props.step) {
    const isSidebarTarget = props.step.target?.includes('data-sidebar-link')
    if (!isSidebarTarget) closeSidebarIfOpen()
    await nextTick()
    setTimeout(updateSpot, 150)
  } else if (!props.active) {
    closeSidebarIfOpen()
  }
}, { immediate: true })

// Recalc on resize
function onResize() {
  isMobile.value = window.innerWidth < 768
  if (props.active) updateSpot()
}

// Recalc on scroll (since we use viewport coords)
function onScroll() {
  if (props.active && props.step?.target) {
    const el = document.querySelector(props.step.target)
    if (el) {
      const rect = el.getBoundingClientRect()
      spot.value = { x: rect.left, y: rect.top, w: rect.width, h: rect.height }
    }
  }
}

onMounted(() => {
  window.addEventListener('resize', onResize)
  window.addEventListener('scroll', onScroll, true)
})
onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('scroll', onScroll, true)
})
</script>

<style>
/* Use global styles (no scoped) so Teleport works correctly */

.onboarding-overlay {
  position: fixed;
  inset: 0;
  z-index: 99999;
  overflow: hidden;
}

.onboarding-mask {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
}

.onboarding-ring {
  position: fixed;
  border: 2.5px solid rgba(99, 102, 241, 0.8);
  border-radius: 14px;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.25), inset 0 0 0 1px rgba(99, 102, 241, 0.1);
  pointer-events: none;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: onboarding-pulse 2s infinite;
}

@keyframes onboarding-pulse {
  0%, 100% { box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.25), inset 0 0 0 1px rgba(99, 102, 241, 0.1); }
  50% { box-shadow: 0 0 0 10px rgba(99, 102, 241, 0.1), inset 0 0 0 1px rgba(99, 102, 241, 0.05); }
}

.onboarding-tooltip {
  position: fixed;
  background: #ffffff;
  border-radius: 16px;
  box-shadow:
    0 24px 48px -12px rgba(15, 23, 42, 0.25),
    0 0 0 1px rgba(99, 102, 241, 0.12),
    0 0 60px -10px rgba(99, 102, 241, 0.15);
  padding: 1.5rem;
  z-index: 100000;
  transition: top 0.4s cubic-bezier(0.4, 0, 0.2, 1),
              left 0.4s cubic-bezier(0.4, 0, 0.2, 1),
              bottom 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  max-height: calc(100vh - 32px);
  overflow-y: auto;
}

.onboarding-tooltip__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.onboarding-tooltip__step-badge {
  display: flex;
  align-items: baseline;
  gap: 0.25rem;
  background: #eef2ff;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
}

.onboarding-tooltip__step-num {
  font-size: 1rem;
  font-weight: 800;
  color: #6366f1;
}

.onboarding-tooltip__step-total {
  font-size: 0.8125rem;
  color: #818cf8;
  font-weight: 500;
}

.onboarding-tooltip__close {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 4px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, color 0.15s;
}
.onboarding-tooltip__close:hover {
  background: #f1f5f9;
  color: #475569;
}

.onboarding-tooltip__title {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.3;
}

.onboarding-tooltip__message {
  margin: 0 0 1rem;
  font-size: 0.9375rem;
  color: #475569;
  line-height: 1.65;
}

.onboarding-tooltip__progress {
  height: 4px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 1rem;
}

.onboarding-tooltip__progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 4px;
  transition: width 0.4s ease;
}

.onboarding-tooltip__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.onboarding-tooltip__footer-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.onboarding-btn {
  padding: 0.5rem 1rem;
  border-radius: 10px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, color 0.15s, transform 0.1s;
  border: none;
  font-family: inherit;
  white-space: nowrap;
}
.onboarding-btn:active {
  transform: scale(0.97);
}

.onboarding-btn--primary {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff;
  box-shadow: 0 2px 8px -2px rgba(99, 102, 241, 0.4);
}
.onboarding-btn--primary:hover {
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  box-shadow: 0 4px 12px -2px rgba(99, 102, 241, 0.5);
}

.onboarding-btn--ghost {
  background: transparent;
  color: #6366f1;
}
.onboarding-btn--ghost:hover {
  background: #eef2ff;
}

.onboarding-btn--skip {
  color: #94a3b8;
  font-weight: 500;
}
.onboarding-btn--skip:hover {
  color: #64748b;
  background: #f1f5f9;
}

/* ====== Mobile responsiveness ====== */
@media (max-width: 767px) {
  .onboarding-tooltip {
    width: calc(100vw - 32px) !important;
    max-width: calc(100vw - 32px) !important;
    padding: 1.25rem;
    border-radius: 16px 16px 16px 16px;
  }

  .onboarding-tooltip__title {
    font-size: 1.125rem;
  }

  .onboarding-tooltip__message {
    font-size: 0.875rem;
    line-height: 1.55;
  }

  .onboarding-tooltip__footer {
    flex-direction: column;
    gap: 0.75rem;
  }

  .onboarding-tooltip__footer-right {
    width: 100%;
    justify-content: space-between;
  }

  .onboarding-btn {
    padding: 0.625rem 1rem;
    font-size: 0.875rem;
  }

  .onboarding-btn--primary {
    flex: 1;
    text-align: center;
  }
}

/* ====== Dark mode ====== */
[data-theme="dark"] .onboarding-tooltip {
  background: #1e293b;
  box-shadow:
    0 24px 48px -12px rgba(0, 0, 0, 0.5),
    0 0 0 1px rgba(99, 102, 241, 0.2),
    0 0 60px -10px rgba(99, 102, 241, 0.1);
}

[data-theme="dark"] .onboarding-tooltip__title {
  color: #f1f5f9;
}

[data-theme="dark"] .onboarding-tooltip__message {
  color: #94a3b8;
}

[data-theme="dark"] .onboarding-tooltip__step-badge {
  background: rgba(99, 102, 241, 0.15);
}

[data-theme="dark"] .onboarding-tooltip__progress {
  background: #334155;
}

[data-theme="dark"] .onboarding-tooltip__close:hover {
  background: #334155;
  color: #e2e8f0;
}

[data-theme="dark"] .onboarding-btn--ghost:hover {
  background: rgba(99, 102, 241, 0.1);
}

[data-theme="dark"] .onboarding-btn--skip {
  color: #64748b;
}
[data-theme="dark"] .onboarding-btn--skip:hover {
  color: #94a3b8;
  background: #334155;
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
