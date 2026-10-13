# Publicação de portfólio

GitHub: https://github.com/horaanjos/AnjosFlow
Netlify: https://anjosflow-horaanjos.netlify.app

Somente o Front-end é publicado. netlify.toml define base frontend, npm run build,
publish dist, Node 24 e VITE_PORTFOLIO_MODE=true. public/_redirects mantém as rotas
React ao atualizar a página. O modo não solicita dados de localhost dos visitantes.

A página inicial apresenta o projeto e capturas reais identificadas. As páginas
permitem explorar formulários; a gravação fica desabilitada. Pomodoro e preferências
funcionam apenas no navegador. O código completo continua disponível para execução
local com Spring Boot e PostgreSQL; não houve implantação da API ou do banco.

Para gerar manualmente a apresentação:

```powershell
cd frontend
$env:VITE_PORTFOLIO_MODE='true'
npm.cmd run build
```

Para voltar ao desenvolvimento local, remova a variável do terminal:

```powershell
Remove-Item Env:VITE_PORTFOLIO_MODE -ErrorAction SilentlyContinue
npm.cmd run dev
```

.env, node_modules, target, dist, .tools e .netlify estão ignorados no Git.
Somente arquivos .env.example sem credenciais são publicados. Não foi implementada
autenticação nem criado recurso de hospedagem de Back-end nesta entrega.

## Atualizações do site

A publicação inicial foi feita manualmente via Netlify CLI. Para novos deploys,
gere o build de portfólio e, da raiz, execute:

`powershell
npm.cmd exec --yes --package=netlify-cli -- netlify deploy --prod --no-build --dir frontend/dist --site 99290c7f-efea-41c9-a529-ec29880080d4
` 

Deploy automático a cada push ainda não está conectado; a integração Git pode
ser ativada no painel Netlify usando o repositório público e netlify.toml.
