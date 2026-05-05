# CaseBridge Platform

Проект для управления командами.

## 📦 Состав
- **Backend**: Java 17 + Spring Boot 3.3.5 (REST API)
- **Frontend**: React + TypeScript + Vite

---

## 🚀 Быстрый старт (Windows)

### 1️⃣ Backend

```powershell
cd backend

# Сборка и запуск (надёжный способ):
.\mvnw.cmd clean package -DskipTests
java -jar target\backend-0.0.1-SNAPSHOT.jar
```

⚠️ Команда `.\mvnw.cmd spring-boot:run` может вызывать `NoClassDefFoundError`.
Если нужен hot-reload: сначала `.\mvnw.cmd dependency:resolve`, затем `spring-boot:run`.

Проверка:  
Откройте [http://localhost:8080/api/teams](http://localhost:8080/api/teams) — должен вернуться пустой массив `[]`.

### 2️⃣ Frontend

```powershell
# Добавить Node.js в PATH текущей сессии (если node/npm не распознаны):
$env:Path = "C:\Program Files\nodejs;" + $env:Path

cd frontend

# Установка зависимостей (обход проблемы с esbuild):
& "C:\Program Files\nodejs\npm.cmd" install --ignore-scripts
& "C:\Program Files\nodejs\node.exe" .\node_modules\esbuild\install.js

# Запуск:
& "C:\Program Files\nodejs\npm.cmd" run dev
```

Откройте в браузере: [http://localhost:5173](http://localhost:5173)

💡 Если `npm run dev` выдаёт ошибку `"node" is not recognized` — убедитесь, что выполнили команду с `$env:Path` выше.
