# Publicar TradeLink nas lojas (sem Mac e sem Android Studio)

Os builds são feitos **na nuvem** pelo GitHub Actions. Você só precisa configurar os **secrets** no repositório e rodar o workflow (ou deixar rodar no push).

---

## Android (Google Play)

### 1. Gerar keystore e base64 (recomendado)

Na pasta **tradelink-frontend**:

```bash
npm run keystore
```

Isso gera:
- `android/tradelink-release.keystore` (não commitar)
- `android/keystore-base64.txt` (conteúdo para o secret ANDROID_KEYSTORE_BASE64)
- `android/GITHUB-SECRETS.txt` (resumo dos valores para os secrets)

**Senha e alias usados:** `tradelink2024` e `tradelink`. Use os mesmos valores nos secrets do GitHub.

### 1b. Alternativa: keytool (se tiver JDK instalado)

```bash
keytool -genkeypair -v -storetype PKCS12 -keystore tradelink-release.keystore -alias tradelink -keyalg RSA -keysize 2048 -validity 10000
```

Depois converta para base64 (PowerShell):  
`[Convert]::ToBase64String([IO.File]::ReadAllBytes("tradelink-release.keystore"))` e salve o resultado.

### 2. Secrets no GitHub

Repositório → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**.

| Nome do secret | Valor |
|----------------|--------|
| `ANDROID_KEYSTORE_BASE64` | A string base64 do arquivo `.keystore` |
| `ANDROID_KEYSTORE_PASSWORD` | Senha do keystore |
| `ANDROID_KEY_ALIAS` | Alias da chave (ex.: `tradelink`) |
| `ANDROID_KEY_PASSWORD` | Senha da chave (pode ser a mesma do keystore) |

### 3. Rodar o build

- **Actions** → workflow **"Build Android (AAB)"** → **Run workflow** (ou faça push em `tradelink-frontend`).
- Ao terminar, em **Artifacts** baixe **android-aab-release**.
- O arquivo dentro é o **App Bundle (.aab)** para enviar na Play Console.

### 4. Enviar na Google Play

1. Acesse [Google Play Console](https://play.google.com/console).
2. Crie o app (se ainda não existir).
3. Em **Produção** (ou teste) → **Criar nova versão** → faça upload do `.aab` que você baixou.

---

## iOS (App Store)

O build roda em um **Mac na nuvem** (GitHub fornece). Você precisa de **conta Apple Developer** (paga) e do **Team ID**.

### 1. Obter o Team ID

- Entre em [Apple Developer](https://developer.apple.com/account) → **Membership** → **Team ID** (10 caracteres).

### 2. Secrets no GitHub

| Nome do secret | Valor |
|----------------|--------|
| `APPLE_DEVELOPMENT_TEAM` | Seu **Team ID** (10 caracteres) |

Só com isso o workflow consegue **fazer o build e o archive**. Para gerar o **IPA assinado** para a App Store, o Xcode usa “Automatically manage signing” com esse Team ID (e a conta configurada no runner). Em contas gratuitas ou em alguns casos, o **export do IPA** pode pedir certificado/perfil manual.

Se o export falhar por causa de assinatura:

- Você pode precisar gerar **certificado de distribuição** e **perfil de provisionamento** no portal Apple, exportar o certificado em `.p12` e o perfil em `.mobileprovision`, converter ambos em base64 e criar os secrets:
  - `IOS_CERTIFICATE_BASE64`
  - `IOS_CERTIFICATE_PASSWORD`
  - `IOS_PROVISIONING_PROFILE_BASE64`

(Se um dia o workflow usar esses secrets, ele importa o certificado e o perfil no keychain do runner e assina o app com eles.)

### 3. Rodar o build

- **Actions** → **"Build iOS (IPA)"** → **Run workflow**.
- Se o export do IPA der certo, em **Artifacts** baixe **ios-ipa** (contém o `.ipa`).

### 4. Enviar na App Store

- Use o [App Store Connect](https://appstoreconnect.apple.com/) e envie o `.ipa` (por exemplo com **Transporter** no Mac, ou pelo Xcode).
- Ou use ferramentas como **fastlane** ou **Codemagic** em cima do IPA gerado.

---

## Resumo

| Plataforma | O que configurar | Onde baixa o build |
|------------|------------------|---------------------|
| **Android** | 4 secrets (keystore em base64 + senhas + alias) | Actions → Artifacts → android-aab-release |
| **iOS** | 1 secret mínimo: `APPLE_DEVELOPMENT_TEAM` | Actions → Artifacts → ios-ipa (se o export rodar) |

Sem secrets de Android, o workflow ainda gera um **AAB debug** (para teste), mas a Play Store exige o **AAB release** assinado com seu keystore.
