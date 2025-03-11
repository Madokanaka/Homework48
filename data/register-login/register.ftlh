<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Регистрация</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="./candidates.css">
</head>
<body class="align-center vh-100 flex">
<div class="container align-center">
    <div class="card shadow p-4 text-center">
        <h2 class="text-primary">Регистрация</h2>

        <form action="/register" method="post">
            <fieldset>
                <legend>Пожалуйста, заполните форму</legend>

                <div class="mb-3">
                    <label for="email">Email</label>
                    <input type="email" class="form-control" id="email" name="email">
                </div>
                <div class="mb-3">
                    <label for="name">Имя</label>
                    <input type="text" class="form-control" id="name" name="name">
                </div>
                <div class="mb-3">
                    <label for="password">Пароль</label>
                    <input type="password" class="form-control" id="password" name="password">
                </div>

                <div class="mb-3 text-danger text-center">
                    <#if error??>
                        <p>${error}</p>
                    </#if>
                </div>

                <button type="submit" class="back w-100">Зарегистрироваться</button>
            </fieldset>
        </form>
    </div>
</div>
</body>
</html>
