<template>
  <div class="app">
    <router-view />
    <ToastContainer />
    <ConfirmModal />
    <CookieConsent />
    <TermsAcceptanceModal />
  </div>
</template>

<script setup>
import ToastContainer from './components/common/ToastContainer.vue'
import ConfirmModal from './components/common/ConfirmModal.vue'
import CookieConsent from './components/common/CookieConsent.vue'
import TermsAcceptanceModal from './components/common/TermsAcceptanceModal.vue'
import { useTheme } from './composables/useTheme'

useTheme() // initialize watchEffect on app load
</script>

<style>
.app {
  min-height: 100vh;
}
</style>
