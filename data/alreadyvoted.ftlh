<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Вы уже проголосовали</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="./candidates.css">
</head>
<body class="align-center vh-100 d-flex justify-content-center align-items-center">
<div class="container text-center">
    <div class="card shadow p-4 d-flex">
        <h2 class="text-danger">Ошибка!</h2>
        <p>Вы уже проголосовали, ${user.name}.</p>
        <p>Вы проголосовали за кандидата: <strong>${candidate.name}</strong>.</p>
        <div class="flex justify-content-center">
            <a href="/" class="back mt-3 ">Вернуться на главную</a>
        </div>
    </div>
</div>
</body>
</html>
