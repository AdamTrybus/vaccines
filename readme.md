# Vaccine Order Management

A system for managing vaccine orders using a microservices architecture. It includes a React frontend and three Spring Boot microservices (`gateway`, `ordering_service`, `producer_service`) that interact with a PostgreSQL database.

## Project Structure

- **frontend**: React app for the user interface.
- **gateway**: Spring Cloud Gateway for routing requests.
- **ordering\_service**: Microservice for managing vaccine orders.
- **producer\_service**: Microservice for managing producers.

## Requirements

To run the project, you need to install the following:

- **Java 17**: For running Spring Boot microservices.
- **Node.js 18+**: For running the React frontend.
- **Maven**: For building Java projects.
- **PostgreSQL**: For the database.
- **Git**: For cloning the repository.

### Installing PostgreSQL

1. Download and install PostgreSQL from [https://www.postgresql.org/download/](https://www.postgresql.org/download/).
2. During installation, set a password for the `postgres` user (e.g., `your_password`).
3. After installation, create a database named `vaccines_db`:
    - Open a PostgreSQL client (e.g., pgAdmin or `psql`) and run:
      ```sql
      CREATE DATABASE vaccines_db;
      ```

## Database Tables

The project uses a PostgreSQL database (`vaccines_db`) with the following table:

- **vaccine\_orders**:
    - `id`: Unique identifier for the order (auto-incremented).
    - `region`: Region where the order is placed (e.g., "Mazowieckie").
    - `cases`: Number of cases reported (e.g., 1000).
    - `vaccine_quantity`: Number of vaccines ordered (e.g., 500).
    - `expected_delivery_time`: Expected delivery date (e.g., "2025-04-08").
    - `status`: Order status (e.g., "PENDING").

Example data:

```sql
INSERT INTO vaccine_orders (region, cases, vaccine_quantity, expected_delivery_time, status)
VALUES ('Mazowieckie', 1000, 500, '2025-04-08', 'PENDING');
```

- **producer\_response**:
    - `id`: Unique identifier for the response (auto-incremented).
    - `order_id`: Reference to the order ID from vaccine_orders (e.g., 7).
    - `vaccine_quantity`: Number of vaccines confirmed by the producer (e.g., 500).
    - `response_date`: Date of the response (e.g., "2025-04-09").
    - `status`: Status of the response (e.g., "CONFIRMED").

Example data:

```sql
INSERT INTO producer_response (order_id, vaccine_quantity, response_date, status)
VALUES (7, 500, '2025-04-09', 'CONFIRMED');
```

## API Endpoints

The microservices expose the following endpoints:

### Ordering Service

- **GET /api/orders**: Retrieve all orders.
- **GET /api/orders/{id}**: Retrieve an order by ID.
- **POST /api/orders**: Create a new order.
    - Example request body:
      ```json
      {
          "region": "Mazowieckie",
          "cases": 1000,
          "vaccineQuantity": 500,
          "expectedDeliveryTime": "2025-04-08"
      }
      ```

### Producer Service

- **POST /api/producers/request**: Send a request to a producer.
    - Example request body:
      ```json
      {
          "orderId": 7,
          "vaccineQuantity": 500
      }
      ```

### Gateway

The gateway routes requests:

- `/api/orders/**` → `ordering_service`
- `/api/producers/**` → `producer_service`

## How to Run the Project

### 1. Clone the Repository

```bash
git clone https://github.com/twoje-konto/vaccines.git
cd vaccines
```

### 2. Configure the Database

Update the database settings in `ordering_service/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vaccines_db
    username: postgres
    password: password
```

### 3. Run the Microservices

Open separate terminal windows for each service:

- **Ordering Service**:
  ```bash
  cd ordering_service
  mvn spring-boot:run
  ```
  Runs on `http://localhost:8081`.
- **Producer Service**:
  ```bash
  cd producer_service
  mvn spring-boot:run
  ```
  Runs on `http://localhost:8082`.
- **Gateway**:
  ```bash
  cd gateway
  mvn spring-boot:run
  ```
  Runs on `http://localhost:8080`.

### 4. Run the Frontend

```bash
cd frontend
npm install
npm start
```

Runs on `http://localhost:3000`.

### 5. Access the Application

- Open `http://localhost:3000` in your browser to use the React app.
- Use the API endpoints (e.g., via Postman) to interact with the backend.

## Testing

- **View Orders**: Open `http://localhost:3000` to see the list of orders.
- **Create an Order**: Use the form on the frontend to create a new order (e.g., Region: "Mazowieckie", Cases: 1000, Vaccine Quantity: 500, Expected Delivery Time: "2025-04-08").
- **Test API**: Use a tool like Postman to send requests to the endpoints (e.g., `POST http://localhost:8080/api/orders`).

## Troubleshooting

- **Network Error**: Ensure all microservices and the database are running.
- **Database Issues**: Verify the database URL, username, and password in `ordering_service` configuration.
- **Port Conflicts**: Check if ports (3000, 8080, 8081, 8082, 5432) are free:
  ```bash
  netstat -aon | findstr :8080
  ```
  
## Create config files in microservices:

- In ordering and producer apps create files 'resources/application.properties'
- In Producer add server.port=8082
- In Ordering add server.port=8081


## Create tables in Postgres:

```bash
docker exec -it <postgres-container-name> psql -U postgres -d mydb

CREATE TABLE vaccine_orders (
    id SERIAL PRIMARY KEY,
    region VARCHAR(255) NOT NULL,
    cases INT NOT NULL,
    vaccine_quantity INT NOT NULL ,
    expected_delivery_time DATE NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE producer_response (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL,
    available_vaccines INT NOT NULL,
    delivery_time VARCHAR(255) NOT NULL,
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES vaccine_orders(id)
);
```