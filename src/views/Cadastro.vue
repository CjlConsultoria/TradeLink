<template lang="pug">
.register-container
  .register-content
    .register-box
      h1.register-title CADASTRO
      p.register-subtitle Complete suas informações para criar sua conta.

      form.register-form(@submit.prevent="goToAddressStep" v-if="currentStep === 1")
        .input-group
          label(for="name") Nome completo
          input#name(type="text" v-model="name" placeholder="Seu nome completo" required)

        .input-group
          label(for="cpf") CPF/CNPJ
          input#cpf(type="text" v-model="cpfCnpj" placeholder="Digite seu CPF ou CNPJ" required)

        .input-group
          label(for="email") Email
          input#email(type="email" v-model="email" placeholder="Seu email profissional" required)

        .input-group
          label(for="telefone") Telefone
          input#telefone(type="text" v-model="telefone" placeholder="(00) 00000-0000" required)

        .input-group
          label(for="password") Senha
          .password-wrapper
            input#password(type="password" v-model="password" placeholder="********" required)
            span.toggle-password(@click="togglePassword('password')")
              span(v-if="showPassword.password" v-html="eyeSlashSvg")
              span(v-else v-html="eyeOpenSvg")

        .input-group
          label(for="confirmPassword") Confirmar senha
          .password-wrapper
            input#confirmPassword(type="password" v-model="confirmPassword" placeholder="********" required)
            span.toggle-password(@click="togglePassword('confirmPassword')")
              span(v-if="showPassword.confirm" v-html="eyeSlashSvg")
              span(v-else v-html="eyeOpenSvg")

        p.register-error(v-if="errorMessage") {{ errorMessage }}
        button.register-button(type="submit") Próximo passo →

      form.register-form(@submit.prevent="handleRegister" v-if="currentStep === 2")
        .input-group
          label(for="cep") CEP
          input#cep(type="text" v-model="cep" placeholder="00000-000" @blur="buscarCep" required)

        .input-group
          label(for="logradouro") Logradouro
          input#logradouro(type="text" v-model="logradouro" placeholder="Rua, Avenida..." required)

        .input-group-inline
          .input-group
            label(for="numero") Número
            input#numero(type="text" v-model="numero" placeholder="123" required)
          .input-group
            label(for="complemento") Complemento
            input#complemento(type="text" v-model="complemento" placeholder="Apto, Bloco..." )

        .input-group
          label(for="bairro") Bairro
          input#bairro(type="text" v-model="bairro" required)

        .input-group-inline
          .input-group
            label(for="cidade") Cidade
            input#cidade(type="text" v-model="cidade" required)
          .input-group
            label(for="estado") Estado
            input#estado(type="text" v-model="estado" required)

        p.register-error(v-if="errorMessage") {{ errorMessage }}
        .register-actions
          button.register-back(type="button" @click="currentStep = 1") ← Voltar
          button.register-button(type="submit") Concluir Cadastro

      .register-success(v-if="registrationSuccess")
        h2 🎉 Cadastro concluído com sucesso!
        p Bem-vindo, {{ name }}!
        button.register-reset(@click="resetForm") Fazer novo cadastro

      p.register-footer Já possui uma conta? 
        a(href="#") Entre agora!

    .map-container
      img.map-image(:src="mapImage" alt="Map")
</template>

<script setup lang="ts">
import { ref } from 'vue'

const mapImage = new URL('../assets/map.png', import.meta.url).href

const currentStep = ref(1)
const registrationSuccess = ref(false)

const name = ref('')
const cpfCnpj = ref('')
const email = ref('')
const telefone = ref('')
const password = ref('')
const confirmPassword = ref('')

const cep = ref('')
const logradouro = ref('')
const numero = ref('')
const complemento = ref('')
const bairro = ref('')
const cidade = ref('')
const estado = ref('')

const errorMessage = ref('')

const showPassword = ref({
  password: false,
  confirm: false
})

const togglePassword = (field: 'password' | 'confirmPassword') => {
  if (field === 'password') {
    showPassword.value.password = !showPassword.value.password
    const input = document.getElementById('password') as HTMLInputElement
    input.type = showPassword.value.password ? 'text' : 'password'
  } else {
    showPassword.value.confirm = !showPassword.value.confirm
    const input = document.getElementById('confirmPassword') as HTMLInputElement
    input.type = showPassword.value.confirm ? 'text' : 'password'
  }
}

const goToAddressStep = () => {
  if (!name.value || !cpfCnpj.value || !email.value || !telefone.value || !password.value || !confirmPassword.value) {
    errorMessage.value = 'Por favor, preencha todos os campos.'
    return
  }
  if (password.value !== confirmPassword.value) {
    errorMessage.value = 'As senhas não conferem.'
    return
  }
  errorMessage.value = ''
  currentStep.value = 2
}

const buscarCep = async () => {
  if (!cep.value) return
  try {
    const response = await fetch(`https://viacep.com.br/ws/${cep.value}/json/`)
    const data = await response.json()
    if (!data.erro) {
      logradouro.value = data.logradouro
      bairro.value = data.bairro
      cidade.value = data.localidade
      estado.value = data.uf
    } else {
      errorMessage.value = 'CEP não encontrado.'
    }
  } catch {
    errorMessage.value = 'Erro ao buscar CEP.'
  }
}

const handleRegister = () => {
  if (!cep.value || !logradouro.value || !numero.value || !bairro.value || !cidade.value || !estado.value) {
    errorMessage.value = 'Preencha todos os campos obrigatórios.'
    return
  }

  errorMessage.value = ''
  registrationSuccess.value = true
}

const resetForm = () => {
  name.value = ''
  cpfCnpj.value = ''
  email.value = ''
  telefone.value = ''
  password.value = ''
  confirmPassword.value = ''
  cep.value = ''
  logradouro.value = ''
  numero.value = ''
  complemento.value = ''
  bairro.value = ''
  cidade.value = ''
  estado.value = ''
  currentStep.value = 1
  registrationSuccess.value = false
}

const eyeOpenSvg = `
<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="#ffffad" stroke-width="2" width="22" height="22">
  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
  <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
</svg>
`

const eyeSlashSvg = `
<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="#ffffad" stroke-width="2" width="22" height="22">
  <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.542-7a10.05 10.05 0 012.479-3.433m3.018-2.11A9.956 9.956 0 0112 5c4.478 0 8.268 2.943 9.542 7a9.956 9.956 0 01-1.238 2.36M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
  <path stroke-linecap="round" stroke-linejoin="round" d="M3 3l18 18"/>
</svg>
`
</script>

<style scoped>
.register-container {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, #1e1e2e, #2c2c3a);
  color: #ffffad;
  font-family: 'Poppins', sans-serif;
}

.register-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 2rem 5rem;
}

.register-box {
  background-color: rgba(255, 255, 255, 0.05);
  padding: 3rem;
  border-radius: 12px;
  width: 450px;
}

.register-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.register-subtitle {
  font-size: 1rem;
  margin-bottom: 2rem;
  opacity: 0.8;
}

.input-group, .input-group-inline {
  display: flex;
  flex-direction: column;
  margin-bottom: 1rem;
}

.input-group-inline {
  flex-direction: row;
  gap: 1rem;
}

.input-group-inline .input-group {
  flex: 1;
}

label {
  margin-bottom: 0.3rem;
  font-size: 0.9rem;
}

input {
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 6px;
  padding: 0.7rem;
  color: #ffffad;
}

.password-wrapper {
  position: relative;
}

.toggle-password {
  position: absolute;
  top: 50%;
  right: 10px;
  transform: translateY(-50%);
  cursor: pointer;
}

.register-button, .register-back, .register-reset {
  background-color: #ffffad;
  color: #1e1e2e;
  border: none;
  padding: 0.8rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: 0.3s;
}

.register-button:hover,
.register-back:hover,
.register-reset:hover {
  background-color: #fff9c4;
}

.register-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 1rem;
}

.register-error {
  color: #ff7575;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.register-success {
  text-align: center;
  animation: fadeIn 0.5s ease-in-out;
}

.register-footer {
  margin-top: 1.5rem;        
  font-size: 0.9rem;
}

.register-footer a {
  color: #ffffad;
  text-decoration: underline;
}

.map-container {
  flex: 1;
  display: flex;
  justify-content: center;
}

.map-image {
  max-width: 90%;
  height: auto;
  opacity: 0.85;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
