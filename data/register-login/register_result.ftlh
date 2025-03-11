<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Результат регистрации</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light d-flex align-items-center justify-content-center vh-100">
<div class="container">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card shadow p-4 text-center">
                <h2 class="text-primary">Регистрация</h2>

                <#if success>
                    <div class="alert alert-success">
                        <p>${message}</p>
                    </div>
                    <a href="/login" class="btn btn-primary">Войти</a>
                <#else>
                    <div class="alert alert-danger">
                        <p>${message}</p>
                    </div>
                    <a href="/register" class="btn btn-warning">Попробовать снова</a>
                </#if>
            </div>
        </div>
    </div>
</div>
</body>
</html>
