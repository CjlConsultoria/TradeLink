<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
    <CotacaoCard v-for="c in cotacoes" :key="c.id != null ? c.id : (c.moeda + '-' + c.parMoeda)" :cotacao="c" :show-refresh="showRefresh" :show-popup="showPopup" />
  </div>
</template>

<script setup>
import CotacaoCard from './CotacaoCard.vue'
defineProps({
  cotacoes: Array,
  showRefresh: { type: Boolean, default: false },
  showPopup: { type: Boolean, default: false }
})
</script>
