<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>Thank you!!</title>
    <link rel="stylesheet" href="./candidates.css">
</head>

<body>
<div class="container flex flex-col align-center">
    <h1>Thank you for your vote!</h1>
    <main class="flex flex-wrap align-center">
        <div class="card">
            <div class="flex flex-col align-center">
                <img src="${candidate.photo}" alt="candidate ${candidate.id}">
                <p><strong>${candidate.name}</strong></p>
                <#if totalVotes == 0>
                    <p>Percentage: 0%</p>
                <#else>
                    <p>Percentage: ${((candidate.votes * 100) / totalVotes)?number?round}%</p>
                </#if>

            </div>
        </div>
    </main>
    <p>-------------------------------------------------------</p>
    <a class="back flex align-center vote-button" href="/votes">Show total votes</a>

    <a class="back flex align-center" href="/">Back to main</a>
</div>
</body>

</html>
