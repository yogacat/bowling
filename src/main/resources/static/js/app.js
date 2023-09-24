document.addEventListener('DOMContentLoaded', function () {
  const createPlayerForm = document.getElementById('createPlayerForm');
  const playerNameInput = document.getElementById('playerName');
  const createPlayerBtn = document.getElementById('createPlayerBtn');

  const submitRollForm = document.getElementById('submitRollForm');
  const playerIdSpan = document.getElementById('playerId');
  const frameNumberSpan = document.getElementById('frameNumber');
  const rollNumberSpan = document.getElementById('rollNumber');
  const pinsInput = document.getElementById('pins');
  const submitRollBtn = document.getElementById('submitRollBtn');

  const nextFrameInfoDiv = document.getElementById('nextFrameInfo');
  const playerScoreStatsDiv = document.getElementById('playerScoreStats');

  // Function to fetch and display player score statistics
  function displayPlayerScoreStatistics(playerId) {
    fetch(`api/players/${playerId}/scores`, {
      method: 'GET',
    })
    .then(response => response.json())
    .then(statistics => {
      playerScoreStatsDiv.innerHTML = `
          <h2>Player Score Statistics</h2>
          <p>Total Score: ${statistics.totalScore}</p>
          <p>Frames:</p>
        <ul>
          ${statistics.frames.map(frame => {
        let frameContent = `Frame ${frame.frameNumber}: `;
        // Check if it's the final score frame
        if (frame.isFinalScore) {
          frameContent += `${frame.score}`;
        }

        if (frame.rolls && frame.rolls.length > 0) {
          frameContent += '<ul>';
          frame.rolls.forEach(roll => {
            if (roll.status) {
              frameContent += `<li>${roll.status}</li>`;
            } else if (roll.pins !== undefined && roll.pins !== null) {
              frameContent += `<li>${roll.pins}</li>`;
            }
          });
          frameContent += '</ul>';
        }

        return `<li>${frameContent}</li>`;
      }).join('')}
        </ul>
        `;
      // Make the div visible
      playerScoreStatsDiv.style.display = 'block';

      // Fetch and display next frame info
      fetch(`api/players/${playerId}/frames`, {
        method: 'GET',
      })
      .then(response => response.json())
      .then(frameInfo => {
        playerIdSpan.textContent = playerId;
        frameNumberSpan.textContent = frameInfo.frameNumber;
        rollNumberSpan.textContent = frameInfo.rollNumber;
      })
      .catch(error => {
        console.error('Error fetching next frame info:', error);
      });
    })
    .catch(error => {
      console.error('Error fetching player score statistics:', error);
    });
  }

  // Event Listener for Create Player Button
  createPlayerBtn.addEventListener('click', function () {
    const playerName = playerNameInput.value;

    fetch('api/players', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({name: playerName}),
    })
    .then(response => response.json())
    .then(data => {
      const playerId = data.id;

      // Display player ID and get next frame info
      nextFrameInfoDiv.textContent = `Player ID: ${playerId}`;
      createPlayerForm.style.display = 'none';
      submitRollForm.style.display = 'block';

      // Fetch and display next frame info
      fetch(`api/players/${playerId}/frames`, {
        method: 'GET',
      })
      .then(response => response.json())
      .then(frameInfo => {
        playerIdSpan.textContent = playerId;
        frameNumberSpan.textContent = frameInfo.frameNumber;
        rollNumberSpan.textContent = frameInfo.rollNumber;

        // Display player score statistics and refresh every 5 seconds (adjust as needed)
        displayPlayerScoreStatistics(playerId);
        setInterval(() => displayPlayerScoreStatistics(playerId), 5000); // Refresh every 5 seconds
      });
    })
    .catch(error => {
      console.error('Error creating player:', error);
    });
  });

  // Event Listener for Submit Roll Button
  submitRollBtn.addEventListener('click', function () {
    const playerId = playerIdSpan.textContent;
    const frameNumber = frameNumberSpan.textContent;
    const rollNumber = rollNumberSpan.textContent;
    const pins = pinsInput.value;

    fetch(`api/players/${playerId}/frames`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        frameNumber: parseInt(frameNumber),
        rollNumber: parseInt(rollNumber),
        pins: parseInt(pins),
      }),
    })
    .then(response => {
      if (response.status === 200) {
        pinsInput.value = '';
      } else {
        console.error('Error submitting roll:', response.statusText);
      }
    })
    .catch(error => {
      console.error('Error submitting roll:', error);
    });
  });
});
