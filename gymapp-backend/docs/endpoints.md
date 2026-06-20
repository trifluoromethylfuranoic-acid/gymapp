
# Endpoints

/api/v1/

## Auth

- /login
  - POST
- /signup/trainee
  - POST
- /signup/trainer
  - POST
- /password
  - PATCH

## Profile

- /profile/**
  - forwards to /tarinees/{username}/** or /tariners/{username}/**

## Trainees

- /trainees
  - GET
- /trainees/{username}
  - GET
  - PATCH
  - DELETE
- /trainees/{username}/trainers
  - GET
  - POST
- /tarinees/{traineeUsername}/trainers/{trainerUsername}
  - DELETE

## Trainers

- /trainers
  - GET
- /trainers/{username}
  - GET
  - PATCH
  - DELETE
- /trainers/{username}/trainees
  - GET
  - POST
- /tariners/{trainerUsername}/trainees/{traineeUsername}
  - DELETE

## Trainings

- /trainings
  - GET
  - POST
- /trainings/{id}
  - GET
  - PATCH
  - DELETE