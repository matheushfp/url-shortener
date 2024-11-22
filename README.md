# Url Shortener

This is a serverless application for URL redirection made with Java using Maven as Build System.<br>
In This project I used AWS Services: Lambda, S3 and API Gateway.<br>
AWS Lambda was used for serverless processing, AWS S3 to store data in buckets and API Gateway to expose endpoints, and centralize the two lambdas on one API.<br>

## Tools
- Java
- Gson
- AWS Lambda
- AWS S3
- AWS API Gateway

## Routes
### POST /create
Send via body: `originalUrl` and `expirationTime` (Epoch Timestamp) to generate a shortUrlCode.

```json
{
    "originalUrl": "https://www.youtube.com/watch?v=sVx1mJDeUjY",
    "expirationTime": "1735095600"
}
```

### GET /{urlCode}
Send `urlCode` param in path. If the requested URL isn't expired you'll be redirected.
