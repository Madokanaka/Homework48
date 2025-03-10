<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>Who's winning?</title>
    <link rel="stylesheet" href="./candidates.css">
</head>

<body>
<div class="container flex flex-col align-center">
    <h1>Who's winning?</h1>
    <main class="flex flex-wrap align-center">
        <#list candidates as candidate>
        <#if candidate == candidates[0]>
        <div class="card winner">
            <#else>
            <div class="card">
                </#if>
                <div class="flex flex-col align-center">
                    <img src="${candidate.photo}" alt="candidate ${candidate.id}">
                    <p>Votes: ${((candidate.votes * 100) / totalVotes)?round}%</p>
                </div>
            </div>
            </#list>
    </main>
    <a class="back flex align-center" href="/">Back to main</a>
</div>
</body>

</html>
