1) запуск : docker compose up

2) postman :
тип запроса - POST - http://localhost:8080/api/adress/process
            - GET - http://localhost:8080/api/adress/all?page=2&size=2 // поддерживает пагинацию
тело json - {
            	"address" : "Спб, Олеко Дундича 5"
            }

3) CodeReview - 2 ветки, нужная ветка finalRefractoring
ветка Main фича , которую делал дополнительно

