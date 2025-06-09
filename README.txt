1) запуск : docker compose up

2) postman :
тип запроса - POST - http://localhost:8080/api/adress/process
            - GET - http://localhost:8080/api/adress?page=2&size=2 // поддерживает пагинацию
тело json - {
            	"adressStartPoint" : "Спб, Олеко Дундича 1", // example start adress
                "adressEndPoint" : "Мск, московская 2" // example end adress
            }

