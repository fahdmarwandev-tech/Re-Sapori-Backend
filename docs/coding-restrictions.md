# Coding Restrictions and Architectural Guidelines

To maintain a clean, performant, and maintainable codebase, all future development must adhere to the following restrictions and guidelines.

## 1. Clean Code Architecture
- **Strict Separation of Concerns**: Follow the Controller -> Service -> Repository flow.
- **Dependency Injection**: Use constructor injection (via Lombok's `@RequiredArgsConstructor`) for all Spring components.
- **DTOs & Entities**: Never expose Entities directly to the client. Always map Entities to Data Transfer Objects (DTOs) at the controller or service boundary.

## 2. Object Mapping
- **MapStruct**: Use MapStruct for all DTO-to-Entity and Entity-to-DTO mappings. Do not write manual mapping logic (e.g., `dto.setName(entity.getName())`) unless absolutely necessary for complex derivations.

## 3. Function Constraints
- **Private Functions**: Extract complex logic or repeated operations into small, descriptive private helper functions.
- **Length Limit**: A single function **must not exceed 20 lines of code**. If a function is longer, it is doing too much and must be broken down into smaller private functions.

## 4. Controller Restrictions
- **No Business Logic**: Controllers must contain **zero business logic**. 
- **Responsibility**: A controller's only responsibility is to:
  1. Receive the HTTP request.
  2. Validate the payload (if applicable).
  3. Call the appropriate Service method.
  4. Return the result wrapped in a `ResponseEntity` with the correct HTTP status code.

## 5. Database & Performance Restrictions
- **Query Limits**: An API endpoint should trigger **1 or 2 database calls at most**.
- **No N+1 Queries**: Ensure JPA relationships are fetched optimally (e.g., using `JOIN FETCH` in repository queries) to prevent N+1 query problems.
- **No For-Loops for DB Calls**: **Never** execute a database call inside a `for` or `while` loop. If you need to fetch or update multiple records, use batch operations or `IN` clauses (e.g., `repository.findAllById(ids)`).
