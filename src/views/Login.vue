  <template lang="pug">
  .login-container
    .login-content
      .login-box
        h1.login-title LOGIN
        p.login-subtitle Bem-vindo de volta! Insira seus dados.

        form.login-form(@submit.prevent="handleLogin")
          .input-group
            label(for="email") Email
            input#email(type="email" v-model="email" placeholder="Entre com seu email" required)

          .input-group
            label(for="password") Senha
            .password-wrapper
              input#password(type="password" v-model="password" placeholder="********" required)
              span.toggle-password(@click="togglePassword")
                span(v-if="showPassword" v-html="eyeSlashSvg")
                span(v-else v-html="eyeOpenSvg")
            a.forgot(href="#") Esqueceu a senha

          // Exibe mensagem de erro se houver
          p.login-error(v-if="errorMessage") {{ errorMessage }}

          button.login-button(type="submit") Entrar

        p.login-footer Ainda não tem uma conta? 
          a(href="#") Cadastre-se gratuitamente!

      .map-container
        img.map-image(:src="mapImage" alt="Map")
  </template>

  <script setup lang="ts">
  import { ref } from 'vue'


  const mapImage = new URL('../assets/map.png', import.meta.url).href

  const email = ref('')
  const password = ref('')
  const showPassword = ref(false)
  const errorMessage = ref('')

  const users = [
    { email: 'usuario@example.com', password: 'senha123' },
    { email: 'admin@site.com', password: 'adminpass' },
  ]

  const handleLogin = () => {
    const user = users.find(u => u.email === email.value)

    if (!user) {
      errorMessage.value = 'Endereço de email não cadastrado.'
      return
    }

    if (user.password !== password.value) {
      errorMessage.value = 'Email ou senha inválidos.'
      return
    }

    errorMessage.value = ''
    alert(`Login realizado com sucesso!\nEmail: ${email.value}`)
  }

  const togglePassword = () => {
    showPassword.value = !showPassword.value
    const input = document.getElementById('password') as HTMLInputElement
    input.type = showPassword.value ? 'text' : 'password'
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
    :root {
  font-family: system-ui, Avenir, Helvetica, Arial, sans-serif;
  line-height: 1.5;
  font-weight: 400;

  color-scheme: light dark;
  color: rgba(255, 255, 255, 0.87);
  background-color: #242424;

  font-synthesis: none;
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

a {
  font-weight: 500;
  color: #646cff;
  text-decoration: inherit;
}
a:hover {
  color: #535bf2;
}

body {
  margin: 0;
  display: flex;
  place-items: center;
  min-width: 320px;
  min-height: 100vh;
}

h1 {
  font-size: 3.2em;
  line-height: 1.1;
}

button {
  border-radius: 8px;
  border: 1px solid transparent;
  padding: 0.6em 1.2em;
  font-size: 1em;
  font-weight: 500;
  font-family: inherit;
  background-color: #1a1a1a;
  cursor: pointer;
  transition: border-color 0.25s;
}
button:hover {
  border-color: #646cff;
}
button:focus,
button:focus-visible {
  outline: 4px auto -webkit-focus-ring-color;
}

.card {
  padding: 2em;
}

#app {
  max-width: 1280px;
  margin: 0 auto;
  padding: 2rem;
  text-align: center;
}

@media (prefers-color-scheme: light) {
  :root {
    color: #213547;
    background-color: #ffffff;
  }
  a:hover {
    color: #747bff;
  }
  button {
    background-color: #f9f9f9;
  }
}

</style>
