# TradeLink – Apps iOS e Android (Capacitor)

Os apps mobile usam o **mesmo frontend Vue** do sistema web, empacotado com **Capacitor**. Ou seja: mesma interface, mesma lógica, mesma API. Depois de alterar o código em `src/`, basta gerar o build e sincronizar para iOS/Android.

## Requisitos

- **Node.js** (já usado no projeto)
- **Android:** [Android Studio](https://developer.android.com/studio) + JDK 17
- **iOS:** **Mac** com [Xcode](https://developer.apple.com/xcode/) (não dá para compilar iOS no Windows)

## Comandos no projeto (`tradelink-frontend`)

| Comando | Descrição |
|--------|-----------|
| `npm run mobile:build` | Gera o build web e copia para os projetos nativos (`dist` → `android` / `ios`) |
| `npm run mobile:android` | Abre o projeto Android no Android Studio |
| `npm run mobile:ios` | Abre o projeto iOS no Xcode (apenas no Mac) |

## Fluxo de trabalho

1. Desenvolva e teste no navegador: `npm run dev`
2. Quando for gerar o app:
   ```bash
   npm run mobile:build
   ```
3. Abra o projeto nativo e rode no dispositivo/emulador:
   - **Android:** `npm run mobile:android` → no Android Studio, Run (▶)
   - **iOS:** no Mac, `npm run mobile:ios` → no Xcode, escolha simulador ou dispositivo e Run (▶)

Sempre que mudar algo em `src/`, rode de novo `npm run mobile:build` antes de abrir o Android Studio ou o Xcode (ou antes de rodar no dispositivo).

## API no app

No app (Capacitor), as chamadas vão direto para a API de produção:  
`https://tradelink-1-ed48.onrender.com/api`  
(definido em `src/api/axiosInstance.js` quando `Capacitor.isNativePlatform()` é verdadeiro).

O backend (profile `render`) já está configurado para aceitar requisições dos apps (CORS com `capacitor://localhost` e `https://localhost`).

## Publicar na loja (sem Android Studio / sem Mac)

Os builds são feitos **na nuvem** pelo GitHub Actions. Você não precisa ter Android Studio nem Mac.

1. No GitHub: **Actions** → workflows **"Build Android (AAB)"** e **"Build iOS (IPA)"**.
2. Configure os **secrets** do repositório (keystore em base64 para Android; Team ID para iOS).
3. Rode o workflow (Run workflow) ou faça push em `tradelink-frontend`.
4. Baixe o **artefato** (AAB ou IPA) na run e envie na Play Console / App Store Connect.

Detalhes completos: **[docs/PUBLICAR-LOJAS.md](PUBLICAR-LOJAS.md)**.

## Estrutura

```
tradelink-frontend/
├── src/                 # Código Vue (web + mobile)
├── dist/                # Build web (gerado por vite build)
├── android/             # Projeto Android (Android Studio)
├── ios/                 # Projeto iOS (Xcode)
└── capacitor.config.json
```

Os diretórios `android/` e `ios/` são gerados e atualizados pelo Capacitor; normalmente são versionados no Git para toda a equipe poder abrir nos IDEs.
