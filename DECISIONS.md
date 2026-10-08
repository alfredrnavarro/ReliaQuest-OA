# Decisions

Fill this in as part of your submission. Brief and specific beats long and
vague — a few sentences per question is plenty. Replace the prompts with your
answers.

## 1. Which endpoints did you add, and why does this system need them?

> List the endpoints you added beyond the three required ones, and say what
> problem each one solves for a consumer of this API.

I added `PATCH /api/v1/employee/{uuid}/terminate`. It sets the employee's `contractTerminationDate` to the current time
and returns the updated employee.

I noticed the Employee model has a termination date, but there was no way to actually set it with the required endpoints. Since this API is supposed to keep employee info in sync with Employees-R-US, they need some way to know when
someone leaves the company. If you try to terminate someone who is already terminated, it returns a 409 so the original
date doesn't get overwritten.

## 2. Did you add any dependency? What does it buy, and what does it cost?

> If you added anything to `build.gradle`, name it and say what you gained and
> what you gave up. If you added nothing, say that — it is a legitimate answer.
>
> Note: Lombok and SLF4J are already on the classpath, so using them is not an
> added dependency.

No, I didn't add anything. I thought about using `spring-boot-starter-validation`, but there are only seven fields to
check, so a few if statements in the service felt simpler and easier to follow. If the model got bigger I would probably
switch to it.

## 3. How did you handle errors and logging, and why those mechanisms?

> Describe the approach you chose for each, and why you chose it over the
> alternatives. For logging, we are interested in what you would want to know
> when this misbehaves in production.

For errors, the service throws a `ResponseStatusException` with the status code and a message. It's already part of Spring, so I didn't need to make my own exception classes, and the controller doesn't need any try/catch. Spring already
handles a bad UUID or broken JSON with a 400 on its own. I also turned on `server.error.include-message` so the client can actually see why their request failed.

For logging I used SLF4J. I log at info when an employee is created or terminated so there's a record of changes, and at warn when a request gets rejected or an employee isn't found. If there were a lot of warnings in production, that
would tell me something is wrong on the client's side. I only log the employee's UUID and not their name, salary or email since that's personal info.

## 4. Where the brief left a decision to you, what did you decide?

> Not everything here is specified, and some of it is under-specified on
> purpose. Where you had to make a call — about the domain model, the shape of a
> request or response, what to validate — say what you chose and why.

1. Required fields are firstName, lastName, salary, age, jobTitle, email and contractHireDate. contractTerminationDate is optional. An empty string counts as missing.
2. Salary can't be negative. That was the one extra check I added because a negative salary doesn't make sense.
3. The server always sets the uuid and fullName. If the client sends them they get overwritten, so nobody can pick their own id or replace an existing employee. fullName is just first name + last name.
4. Status codes: 201 when an employee is created, 400 for bad input or an invalid UUID, 404 if the UUID doesn't exist, and 409 if you terminate someone twice.
5. I stored employees in a `ConcurrentHashMap` with the UUID as the key, so looking someone up is fast and it's safe if lots of requests come in at the same time. It starts with two mock employees.

## 5. What did you deliberately choose *not* to do?

> The most useful question here. What did you consider and decide against, and
> what would make you revisit it? Scope you declined on purpose is not a gap.

1. No authentication, since the brief said not to add an auth provider. In a real app this would need it.
2. No update or delete endpoints. Terminating keeps the employee's record instead of deleting it. I would add an update endpoint if they needed to change things like job title or salary.
3. No checks on email format or age range. I kept validation to what the brief asked for plus salary, and would add more once I knew the actual rules.
4. No separate request class. The POST body maps straight to `EmployeeImpl`. If the request and response started to look different I would split them up.
5. No custom exceptions or global exception handler, because `ResponseStatusException` already covers everything here.
6. No pagination on the get all endpoint since there's only a small amount of data in memory. I'd add it if this used a real database.
