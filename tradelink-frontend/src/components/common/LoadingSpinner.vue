<template>
  <div class="loading-spinner" :class="[`loading-spinner--${size}`]" role="status" :aria-label="text || 'Carregando'">
    <span class="loading-spinner__dot"></span>
    <span v-if="text" class="loading-spinner__text">{{ text }}</span>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: '' },
  size: { type: String, default: 'md', validator: v => ['sm', 'md', 'lg'].includes(v) }
})
</script>

<style scoped>
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 3rem 1rem;
}

.loading-spinner--sm {
  padding: 1.5rem 0.5rem;
}
.loading-spinner--sm .loading-spinner__dot {
  width: 1.5rem;
  height: 1.5rem;
  border-width: 2px;
}
.loading-spinner--sm .loading-spinner__text {
  font-size: 0.75rem;
}

.loading-spinner--lg {
  padding: 4rem 1rem;
}
.loading-spinner--lg .loading-spinner__dot {
  width: 3.5rem;
  height: 3.5rem;
  border-width: 4px;
}

.loading-spinner__dot {
  width: 2.5rem;
  height: 2.5rem;
  border: 3px solid rgb(var(--tl-border));
  border-top-color: rgb(var(--tl-primary));
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.loading-spinner__text {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
