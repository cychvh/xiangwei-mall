# AGENTS.md

## Project context
- Frontend: Vue 3 + Element Plus
- Existing request wrapper: search for request.js / axios instance and reuse it

## Setup commands
- Install: pnpm install (if pnpm-lock.yaml exists; otherwise npm install)
- Dev: pnpm dev (or npm run dev)
- Build: pnpm build
- Lint (if configured): pnpm lint

## API source of truth
- Primary: docs/openapi.json (if present)
- Secondary: docs/api-report.md
- Do not invent endpoints/fields. If unclear, mark TBD and point to missing info.

## Conventions
- Base URL from env (VITE_API_BASE_URL)
- Auth header: Authorization: Bearer <token>
- Unified response wrapper: Result { code, message, data } (if backend differs, follow backend docs)

## Safety
- Ask before adding new dependencies
- Prefer minimal changes; do not delete existing pages without explicit instruction
