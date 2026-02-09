/**
 * Gera o keystore Android (PKCS12) e o base64 para o GitHub Secret ANDROID_KEYSTORE_BASE64.
 * Rode: node scripts/generate-keystore.cjs
 * Requer: npm install node-forge
 */
const forge = require('node-forge');
const fs = require('fs');
const path = require('path');

const PASSWORD = 'tradelink2024';
const ALIAS = 'tradelink';
const KEYSTORE_PATH = path.join(__dirname, '..', 'android', 'tradelink-release.keystore');
const BASE64_PATH = path.join(__dirname, '..', 'android', 'keystore-base64.txt');
const SECRETS_PATH = path.join(__dirname, '..', 'android', 'GITHUB-SECRETS.txt');

console.log('Gerando par de chaves RSA 2048...');
const keys = forge.pki.rsa.generateKeyPair(2048);

console.log('Criando certificado autoassinado...');
const cert = forge.pki.createCertificate();
cert.publicKey = keys.publicKey;
cert.serialNumber = '01';
cert.validity.notBefore = new Date();
cert.validity.notAfter = new Date();
cert.validity.notAfter.setFullYear(cert.validity.notBefore.getFullYear() + 25);
const attrs = [
  { name: 'commonName', value: 'TradeLink' },
  { name: 'organizationName', value: 'CJL Consultoria' },
  { shortName: 'OU', value: 'App' },
  { shortName: 'L', value: 'Sao Paulo' },
  { shortName: 'ST', value: 'SP' },
  { shortName: 'C', value: 'BR' }
];
cert.setSubject(attrs);
cert.setIssuer(attrs);
cert.setExtensions([
  { name: 'basicConstraints', cA: true },
  { name: 'keyUsage', keyCertSign: true, digitalSignature: true, keyEncipherment: true }
]);
cert.sign(keys.privateKey, forge.md.sha256.create());

console.log('Empacotando em PKCS12...');
const p12Asn1 = forge.pkcs12.toPkcs12Asn1(keys.privateKey, cert, PASSWORD, {
  friendlyName: ALIAS,
  useMac: true
});
const p12Der = forge.asn1.toDer(p12Asn1).getBytes();
const p12Binary = Buffer.from(p12Der, 'binary');

const androidDir = path.join(__dirname, '..', 'android');
if (!fs.existsSync(androidDir)) fs.mkdirSync(androidDir, { recursive: true });

fs.writeFileSync(KEYSTORE_PATH, p12Binary);
console.log('Keystore salvo:', KEYSTORE_PATH);

const base64 = p12Binary.toString('base64');
fs.writeFileSync(BASE64_PATH, base64, 'utf8');
console.log('Base64 salvo:', BASE64_PATH);

const secretsText = `Cole estes valores nos Secrets do GitHub (Settings → Secrets and variables → Actions):

ANDROID_KEYSTORE_BASE64 = (conteúdo do arquivo keystore-base64.txt - copie tudo)
ANDROID_KEYSTORE_PASSWORD = ${PASSWORD}
ANDROID_KEY_ALIAS = ${ALIAS}
ANDROID_KEY_PASSWORD = ${PASSWORD}

O arquivo keystore-base64.txt contém uma linha única (base64). Copie o conteúdo completo.
Não commite keystore-base64.txt nem tradelink-release.keystore (já estão no .gitignore).
`;
fs.writeFileSync(SECRETS_PATH, secretsText, 'utf8');
console.log('Instruções salvas:', SECRETS_PATH);
console.log('\nPronto. Abra android/keystore-base64.txt e copie todo o conteúdo para o secret ANDROID_KEYSTORE_BASE64.');
console.log('Senha e alias para os outros secrets:', PASSWORD, '|', ALIAS);
console.log('\nArquivo GITHUB-SECRETS.txt tem o resumo.');
