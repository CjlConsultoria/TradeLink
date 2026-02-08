<template>
  <button
    type="button"
    class="fab"
    aria-label="Nova recomendação"
    @click="$emit('click')"
  >
    <span class="fab__icon">+</span>
  </button>
</template>

<script setup>
defineEmits(['click'])
</script>

<style scoped>
.fab {
  position: fixed;
  bottom: 1.5rem;
  right: 1.5rem;
  width: 3.5rem;
  height: 3.5rem;
  border-radius: 50%;
  background: rgb(var(--tl-primary));
  color: white;
  border: none;
  box-shadow: 0 4px 14px rgba(var(--tl-primary), 0.4);
  cursor: pointer;
  z-index: 40;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s, box-shadow 0.2s, background 0.2s;
}

.fab:hover {
  background: rgb(var(--tl-primary-hover));
  box-shadow: 0 6px 20px rgba(var(--tl-primary), 0.45);
  transform: scale(1.05);
}

.fab:active {
  transform: scale(0.98);
}

.fab:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(var(--tl-primary), 0.3);
}

.fab__icon {
  font-size: 1.75rem;
  font-weight: 300;
  line-height: 1;
}
</style>
