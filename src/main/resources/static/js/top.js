// top.js
document.addEventListener('DOMContentLoaded', function () {
  const topScoresDiv = document.getElementById('topScores');

  // Function to fetch and display top scores
  function displayTopScores() {
    fetch('api/scores', {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
    })
    .then(response => response.json())
    .then(scores => {
      topScoresDiv.innerHTML = `
        <h2>Top Scores</h2>
        <table>
          <thead>
            <tr>
              <th>Player Name</th>
              <th>Total Score</th>
            </tr>
          </thead>
          <tbody>
            ${scores.map(score => `
              <tr>
                <td>${score.name}</td>
                <td>${score.totalScore}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      `;
    })
    .catch(error => {
      console.error('Error fetching top scores:', error);
    });
  }

  // Call the function to initially display top scores
  displayTopScores();
});
