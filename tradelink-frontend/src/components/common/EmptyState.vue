<template>
  <div class="empty-state">
    <p class="empty-state__message">{{ message }}</p>
    <slot />
  </div>
</template>

<script setup>
defineProps({ message: { type: String, default: 'Nenhum item encontrado' } })
</script>

<style scoped>
.empty-state {
  text-align: center;
  padding: 2.5rem 1rem;
  color: rgb(var(--tl-text-muted));
}

.empty-state__message {
  font-size: 1rem;
  line-height: 1.5;
  margin: 0;
}
</style>
