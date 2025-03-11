<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Результат регистрации</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="./candidates.css">
</head>
<body class="align-center vh-100 flex">
<div class="container align-center">
    <div class="card text-center shadow p-4">
        <h2 class="text-primary">Регистрация</h2>

        <#if success>
            <div class="alert alert-success">
                <p>${message}</p>
            </div>
            <a href="/login" class="back">Войти</a>
        <#else>
            <div class="alert alert-danger">
                <p>${message}</p>
            </div>
            <a href="/register" class="back mx-auto d-block">Попробовать снова</a>
        </#if>
    </div>
</div>
</body>
</html>
