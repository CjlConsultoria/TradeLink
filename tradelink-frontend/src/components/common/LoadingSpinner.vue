<template>
  <div class="loading-spinner" role="status" aria-label="Carregando">
    <span class="loading-spinner__dot"></span>
  </div>
</template>

<style scoped>
.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
}

.loading-spinner__dot {
  width: 2.5rem;
  height: 2.5rem;
  border: 3px solid rgb(var(--tl-border));
  border-top-color: rgb(var(--tl-primary));
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
