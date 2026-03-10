<template>
  <div class="app">
    <router-view />
    <ToastContainer />
    <ConfirmModal />
  </div>
</template>

<script setup>
import ToastContainer from './components/common/ToastContainer.vue'
import ConfirmModal from './components/common/ConfirmModal.vue'
</script>

<style>
.app {
  min-height: 100vh;
}
</style>
