# Bowling Score Board

This is a Spring Boot application that counts score for Bowling game.

## Setup

To run the application you need to

- start the database by executing `docker-compose up`
- start the application itself
- open `http://localhost:8066` in your browser

## Interface

There are only two pages

- `index.html` used for the player to insert and calculate his score
- `top.html` score board with top scores, not limited

## What is supported

Basic game calculation. By default, I suppose there are 5 lanes where players can play.
At the backend there is a REST API that can handle more than one player, depending on a
`bowling.lines` property in the `application.properties` file.

In memory database allows to test the functionality of the application without writing to the actual database.
Integration tests are implemented. There are several tests for different scenarios including a perfect game.

Exceptions (though not all as I didn't finish) will be transformed to JSON format and sent by the API in the response
body.

There are minimal logs.

## What is not supported

There is no recovery if the game was not finished, it is not supported by the frontend as I am a backend developer
without a deep frontend knowledge. The API has a possibility to continue the game, though the endpoint to find out about
all the players who did not finish their games is not implemented.

Deletion is not supported. Neither deletion of the player, nor deletion of the roll/frame. You cannot edit the roll you
already saved.

Sessions, security, optimized frontend to avoid too many calls to the API.

## Algorithm

I save each roll and update the frame score with the pins, that were knocked off in all the rolls of the frame. After
this there might be some frames, that are "finalized".

There are three kinds of frames:

- regular frame
- one that has a strike
- one that has a spare

After a while a list of these unfinished frames will grow and some of them can be finalized. I count the last finished
score while doing this. If the frame was finalized, I set its value to the last finished score.

Regular frame has no dependencies and can be finalized immediately. Frames with strike and spare wait for the next
two/one rolls of the next frame to be finalized.

After all the frames are finalised, the game is finished and the score is updated.

## Next steps

As I ran out of free time I can spend on this task there are things I did not implement.

However, what could be done next is this:

- improved logging - logging exceptions in Services, info logs to see what is happening
- improved tests - there are several controller endpoints left uncovered by tests, though service functionality is
  tested
- optimization of UI calls - there might be redundant calls made to the API from the frontend, with the frontend
  knowledge I am sure it can be minimized.
- adding a possibility to continue a game when a player maybe accidentally closed the page
- adding a possibility to edit the last roll in case of a mistake