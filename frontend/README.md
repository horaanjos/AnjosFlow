# AnjosFlow Front-end

React/TypeScript/Vite/Tailwind com dados reais da API. Execute de frontend:

```powershell
npm.cmd ci
npm.cmd run dev
```

Abra http://localhost:5173, com Back-end em http://localhost:8080.
VITE_API_URL pode ser definido em frontend/.env; não inclua credenciais.

```powershell
npm.cmd test
npm.cmd run typecheck
npm.cmd run build
npm.cmd run test:e2e
```

E2E exige API/Vite iniciados e Microsoft Edge. Componentes recebem DTOs tipados;
serviços centralizam HTTP e horários. Preferências/cronômetro usam localStorage;
disciplinas e todos os dados de estudo usam PostgreSQL via API.
Calendário e gráficos são carregados por rota para reduzir o bundle inicial.

A imagem oficial foi copiada fielmente para public/anjosflow-logo.png.
Mais informações na documentação da raiz e em docs/business-rules.md.
## Apresentação na Netlify

https://anjosflow-horaanjos.netlify.app

VITE_PORTFOLIO_MODE=true ativa a apresentação pública sem chamadas à API local.
Pomodoro e preferências são locais ao navegador; cadastros não são gravados.
O desenvolvimento padrão continua integrado ao Back-end. Veja docs/deployment.md.
