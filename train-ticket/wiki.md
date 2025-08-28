# all Services


### ts-admin-basic-info-service

Summary:
This service provides CRUD APIs to manage basic information for administrator, include contacts information, station information, train information, config information and price information.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
|/adminbasic/contacts | GET | Get all contacts information |
|/adminbasic/contacts/{contactsId}| DELETE | Delete contacts by Id |
|/adminbasic/contacts | PUT | Modify contacts information |
|/adminbasic/contacts | POST | Add contacts |
|/adminbasic/stations | GET | Get all stations information |
|/adminbasic/stations/{id} | DELETE | Delete stations by Id |
|/adminbasic/stations| PUT | Modify stations information |
|/adminbasic/stations| POST | Add stations |
|/adminbasic/trains | GET | Get all trains information |
|/adminbasic/trains/{id} | DELETE | Delete trains by Id |
|/adminbasic/trains | PUT | Modify trains information |
|/adminbasic/trains | POST | Add trains |
|/adminbasic/configs | GET | Get all configs information |
|/adminbasic/configs/{name} | DELETE | Delete configs by Name |
|/adminbasic/configs | PUT | Modify configs information |
|/adminbasic/configs | POST | Add configs |
|/adminbasic/prices | GET | Get prices information |
|/adminbasic/prices/{pricesId} | DELETE | Delete prices by Id |
|/adminbasic/prices | PUT | Modify prices information |
|/adminbasic/prices | POST | Add stations |


***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-contacts-service | /api/v1/contactservice/contacts | GET | Get Contacts information |
| ts-contacts-service | /api/v1/contactservice/contacts/ + contactsId | DELETE |  Delete One Contact |
| ts-contacts-service | /api/v1/contactservice/contacts | PUT | Modify Contacts information |
| ts-contacts-service | /api/v1/contactservice/contacts/admin | POST | add contacts |
| ts-station-service | /api/v1/stationservice/stations | GET | get stations information |
| ts-station-service | /api/v1/stationservice/stations | POST | add station |
| ts-station-service | /api/v1/stationservice/stations/ + id | DELETE | Delete One Station |
| ts-station-service | /api/v1/stationservice/stations | PUT | Modify Stations information |
| ts-train-service | /api/v1/trainservice/trains | GET | get all trains information |
| ts-train-service | /api/v1/trainservice/trains | POST | add train |
| ts-train-service | /api/v1/trainservice/trains/ + id | DELETE | Delete one train |
| ts-train-service | /api/v1/trainservice/trains | PUT | Modify Trains information |
| ts-config-service | /api/v1/configservice/configs | GET | get config information |
| ts-config-service | /api/v1/configservice/configs | POST | add config |
| ts-config-service | /api/v1/configservice/configs / + id | DELETE | Delete one config |
| ts-config-service | /api/v1/configservice/configs | PUT | Modify Configs information |
| ts-price-service | /api/v1/priceservice/prices | GET | get price information |
| ts-price-service | /api/v1/priceservice/prices | POST | add price|
| ts-price-service | /api/v1/priceservice/prices/ + pricesId | DELETE | Delete one price|
| ts-price-service | /api/v1/priceservice/prices | PUT | Modify Prices information |



### ts-admin-order-service
Summary: 
This service provide CRUD APIs about order management for administrator.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /adminorder | GET | Get all orders |
| /adminorder | POST | Add one order |
| /adminorder | PUT | Modify and Update Order |
| /adminorder/{orderId}/{trainNumber} | DELETE | delete one order |

***

Main Invocations:
ts-order-service: orders about train which trainNumber start with "G" or "D" 
ts-order-other-service: orders about train which trainNumber **NOT** start with "G" or "D" 

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-order-service | /api/v1/orderservice/order | GET | Get G or D order information
| ts-order-other-service | /api/v1/orderOtherService/orderOther | GET | Get other order information   
| ts-order-service | /api/v1/orderservice/order/+orderId | DELETE | delete order's information. 
| ts-order-other-service | /api/v1/orderOtherService/orderOther/"+orderId | DELETE | delete other order's information. 
| ts-order-service | /api/v1/orderservice/order/admin | PUT | modify order information 
| ts-order-other-service | /api/v1/orderOtherService/orderOther/admin | PUT | modify other order information 
| ts-order-service | /api/v1/orderservice/order/admin | POST | add order 
| ts-order-other-service | /api/v1/orderOtherService/orderOther/admin | POST | add other order 




### ts-admin-route-service

Summary: 
This service provide APIs to manage route. The route entity include distances, stations, start station and end station.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/adminrouteservice/adminroute | GET |get all routes' information |
| /api/v1/adminrouteservice/adminroute | POST | add one route |
| /api/v1/adminrouteservice/adminroute/{routeId} | DELETE | delete one route |



***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-route-service | /api/v1/routeservice/routes | GET | get all routes' information |
| ts-route-service | /api/v1/routeservice/routes | POST | create or modify route |
| ts-route-service | /api/v1/routeservice/routes/+routeId | DELETE | delete one route |
| ts-station-service | /api/v1/stationservice/stations/idlist | POST | check if stations exist |


### ts-admin-travel-service

Summary: 
This service provide APIs to manage teavel. The travel entity include the type of train, the trip and the start and end time.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/admintravelservice/admintravel | GET | get all travel information |
| /api/v1/admintravelservice/admintravel | POST | add one travel entity |
| /api/v1/admintravelservice/admintravel | PUT | update one travel's information |
| /api/v1/admintravelservice/admintravel/{tripId} | DELETE | delete a travel entity |

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-travel-service | /api/v1/travelservice/admin_trip | GET | get trip information from ts-travel-service |
| ts-travel2-service | /api/v1/travel2service/admin_trip | GET | get trip information from ts-travel2-service |
| ts-travel-service | /api/v1/travelservice/trips | POST | add G or D trip entity |
| ts-travel2-service | /api/v1/travel2service/trips | POST | Add other trip entity |
| ts-travel-service | /api/v1/travelservice/trips | PUT | modify G or D trip information |
| ts-travel2-service | /api/v1/travel2service/trips | PUT | modify other trip information |
| ts-travel-service | /api/v1/travelservice/trips + tripId | DELETE | delete G or D trip entity |
| ts-travel2-service | /api/v1/travel2service/trips + tripId | DELETE | delete other trip entity |
| ts-station-service | /api/v1/stationservice/stations/idlist | POST | check if stations exist |
| ts-train-service | /api/v1/trainservice/trains/byName/ + trainTypeName | GET | get train type by train type name |
| ts-route-service | /api/v1/routeservice/routes/ + routeId | GET | get route by id |


### ts-admin-user-service

Summary: 
This service provide APIs to manage users, include get user information, add user, modify user information and delete a user.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/adminuserservice/users | GET | get all user information |
| /api/v1/adminuserservice/users | PUT | update user information |
| /api/v1/adminuserservice/users | POST | add one user entity |
| /api/v1/adminuserservice/users/{userId} | DELETE | delete one user entity |

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-user-service | /api/v1/userservice/users | GET | get all user information |
| ts-user-service | /api/v1/userservice/users | PUT | update user information |
| ts-user-service | /api/v1/userservice/users | POST | add one user entity |
| ts-user-service | /api/v1/userservice/users | DELETE | delete one user entity |



### ts-assurance-service
Summary: 
This service provide APIs to manage and buy assurance, include many get APIs to get assurance information by different parameter, delete APIs to cancel assurance order and PATCH API to modify assurance order.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/assuranceservice/assurances | GET | get all assurance information. |
| /api/v1/assuranceservice/types | GET | get assurance types |
| /api/v1/assuranceservice/assurances/assuranceid/{assuranceId} | GET | get one assurance information by it's id |
| /api/v1/assuranceservice/assurances/orderid/{orderId} | GET | get assurance information by orderId |
| /api/v1/assuranceservice/assurances/{assuranceId}/{orderId}/{typeIndex} | **PATCH** | modify assurance information. |
| /api/v1/assuranceservice/assurances/{typeIndex}/{orderId} | GET | **create** assurance entity |
| /api/v1/assuranceservice/assurances/assuranceid/{assuranceId} | GET | find assurance by id |
| /api/v1/assuranceservice/assurance/orderid/{orderId} | GET | find assurance by orderid |

***

Main Invocations:
The service CRUD informations via database, no other Invocations. 


### ts-auth-service
Summary: 
This service provide APIs to manage user informations and auth operation.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/auth | POST | create user |
| /api/v1/users/login | POST | login check and dispatch token to user |
| /api/v1/users | GET | get all user information |
| /api/v1/users/{userId} | DELETE | delete one user entity |

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-verification-code-service | /api/v1/verifycode/verify/ + verifyCode | GET | get verifycode picture |



### ts-basic-service
Summary: 
This service provide APIs to query some basic information: basic travel information and basic station information. 

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/basicservice/basic/travel| POST | query basic travel information |
| /api/v1/basicservice/basic/{stationName} | GET | query stationId by stationName |

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-station-service | /api/v1/stationservice/stations/id/ + stationName | GET | query for stationId |
| ts-station-service | /api/v1/stationservice/stations/id/ + stationName | GET | check if this station exists |
| ts-train-service | /api/v1/trainservice/trains/ + trainTypeId | GET | query train type by trainTypeId
| ts-route-service | /api/v1/routeservice/routes/ + routeId | GET | get route information by routeId |
| ts-price-service | /api/v1/priceservice/prices/ + routeId + / + trainType | GET | query price by routeId and trainType |


### ts-cancel-service

Summary: 

This service provide APIs to calculate refund and cancel ticket. 

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/cancelservice/cancel/refound/{orderId} | GET | calculate refund amount. |
| /api/v1/cancelservice/cancel/{orderId}/{loginId} | GET | cancel ticket |

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-notification-service | /api/v1/notifyservice/notification/order_cancel_success | POST | send email if cancel success |
| ts-order-service | /api/v1/orderservice/order | PUT | cancel order |
| ts-order-other-service | /api/v1/orderOtherService/orderOther | PUT | cancel other order |
| ts-inside-payment-service | /api/v1/inside_pay_service/inside_payment/drawback/ + userId + / + money | GET | drawback money |
| ts-user-service | /api/v1/userservice/users/id/+orderId | GET | get user account |
| ts-order-service | /api/v1/orderservice/order/+orderId | GET | get order information by orderId |
| ts-order-other-service | /api/v1/orderOtherService/orderOther/ + orderId | GET | get other order information by orderId |



### ts-common
Summary: 
It provides normal utils functions and exceptions, don't provide APIs.


### ts-config-service
Summary: 

This service provide APIs to manage configs, include query, create, update and delete.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| api/v1/configservice/configs | GET | query config informations |
| api/v1/configservice/configs | POST | create config entity |
| api/v1/configservice/configs | PUT | update config informations |
| api/v1/configservice/configs/{configName} | DELETE | delete config entity |
| api/v1/configservice/configs/{configName} | GET | query config entity by configName |



***

Main Invocations:

This service CRUD informations via database, no other Invocations. 



### ts-consign-price-service
Summary: 
This service provide APIs to calculate consign price and manage consign config.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/consignpriceservice/consignprice/{weight}/{isWithinRegion} | GET | get confignprice by weight and region |
| /api/v1/consignpriceservice/consignprice/price | GET | query price information |
| /api/v1/consignpriceservice/consignprice/config| GET | query consign price config |
| /api/v1/consignpriceservice/consignprice | POST | modify consignprice |



***

Main Invocations:

This service CRUD informations via database, no other Invocations. 



### ts-consign-service

Summary: 
This service provide APIs to create, update and query consign order. 

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/consignservice/consigns | POST | create consign |
| /api/v1/consignservice/consigns | PUT | update consign |
| /api/v1/consignservice/consigns/account/{id} | GET | query consign by accountId |
| /api/v1/consignservice/consigns/order/{id} | GET | query consign by OrderId |
| /api/v1/consignservice/consigns/{consignee} | GET | query consign by consignee |

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-consign-price-service | /api/v1/consignpriceservice/consignprice/ + weight + area | GET | calculate consign price |



### ts-contacts-service
Summary: 
This service provide APIs to get all contacts, create contacts, create contacts in admin, delete contact, modify contact's information, query contacts by accountId adn query contacts by contactId.



***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| api/v1/contactservice/contacts | GET  | get all contacts |
| api/v1/contactservice/contacts  | POST |create contact  |
| api/v1/contactservice/contacts/admin | POST |create contacts in admin  |
| api/v1/contactservice/contacts/{contactsId} | DELETE | delete contacts by contactsId |
| api/v1/contactservice/contacts | PUT |modify contacts |
| api/v1/contactservice/contacts/account/{accountId} |GET | query contacts by account Id |
| api/v1/contactservice/contacts/{id} | GET |query contacts by contactId |

***

Main Invocations:

This service CRUD informations via database, no other Invocations. 


### ts-execute-service

Summary:
This service provide ticket collection and execute related API.



***
Main APIs:

| URI  |  Http Method | Description | 
| --- | --- | --- |
| /api/v1/executeservice/execute/execute/{orderId} | GET | ticket execute API 
| /api/v1/executeservice/execute/collected/{orderId} | GET | ticket collected API 



***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-order-service | /api/v1/orderservice/order/status/ + orderId + / + status | GET | execute order (G and D order)|
| ts-order-other-service | /api/v1/orderOtherService/orderOther/status/ + orderId + / + status | GET | execute other order |
| ts-order-service | /api/v1/orderservice/order/ + orderId | GET | get order by id |
| ts-order-other-service | /api/v1/orderOtherService/orderOther/ + orderId | GET | get other order by id |


### ts-food-map-service
Summary: 
This service provide foodstoreMap and trainfoodMap APIs.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/foodmapservice/foodstores | GET | get all food store map in all stations |
| /api/v1/foodmapservice/foodstores/{stationId} | GET | get food store map in one station |
| /api/v1/foodmapservice/foodstores | POST | get food stores by station list |
| /api/v1/foodmapservice/trainfoods | GET | get food on train |
| /api/v1/foodmapservice/trainfoods/{tripId} | GET | get trainfood in a trip |

***

Main Invocations:
This service CRUD informations via database, no other Invocations. 



### ts-food-service

Summary:
This service provide APIs to CRUD foodOrders, get all food order and get all foods in an interval.



***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/foodservice/orders | GET | get all foodorder |
| /api/v1/foodservice/orders | POST | create a foodorder |
| /api/v1/foodservice/createOrderBatch| POST | create food orders in batch |
| /api/v1/foodservice/orders | PUT | update a food order |
| /api/v1/foodservice/orders/{orderId} | DELETE | delete a order |
| /api/v1/foodservice/orders/{orderId} | GET | find order by foodid |
| /api/v1/foodservice/foods/{date}/{startStation}/{endStation}/{tripId} | GET | get all foods |

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-train-food-service | /api/v1/trainfoodservice/trainfoods/ + tripId | GET | get train food list |
| ts-travel-service | /api/v1/travelservice/routes/ + tripId | GET | get the station through which the train passes |
| ts-station-food-service | /api/v1/stationfoodservice/stationfoodstores | POST | get station food stores by stationNames |



### ts-inside-payment-service


Summary:
This service provide APIs to do inside payment, query money, top up money into account, query money info, create account.



***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/inside_pay_service/inside_payment | POST | pay for the order |
| /api/v1/inside_pay_service/inside_payment/account | POST | create account |
| /api/v1/inside_pay_service/inside_payment/{userId}/{money} | GET | top-up money |
| /api/v1/inside_pay_service/inside_payment/payment | GET | query payment |
| /api/v1/inside_pay_service/inside_payment/account | GET | query account |
| /api/v1/inside_pay_service/inside_payment/drawback/{userId}/{money} | GET | drawback money to user |
| /api/v1/inside_pay_service/inside_payment/difference | POST | pay difference |
| api/v1/inside_pay_service/inside_payment/money | GET | query money |

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-order-service | /api/v1/orderservice/order/ + orderId | GET | get order info by id |
| ts-order-other-service | /api/v1/orderOtherService/orderOther/status/ + orderId + / + orderStatus | GET | get other order info by id |
| ts-payment-service | /api/v1/paymentservice/payment | POST | call third-party payment |


### ts-news-service

Summary:
This service only have test content.


### ts-notification-service
Summary:
This service provide API to send email when preserve success, order create success, update order success and cancel notification success.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/notifyservice/notification/preserve_success | POST | preserve success, send email notification |
| /api/v1/notifyservice/notification/order_create_success | POST | create order success, send email notification |
|/api/v1/notifyservice/notification/order_changed_success | POST  |update order success, send email notification |
| /api/v1/notifyservice/notification/order_cancel_success | POST | cancel notification success, send email notification |



***

Main Invocations:

No Invocations of other service.




### ts-order-other-service
Summary:
This service provide API to manage other order(means order which train's ID is not start with G or D)

**(There's some RESTful problems in this service for now.)**

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/orderOtherService/orderOther/tickets | **POST** | get ticket list by date and trip id |
| /api/v1/orderOtherService/orderOther | POST | create order |
| /api/v1/orderOtherService/orderOther/admin | POST |admin create new order |
| /api/v1/orderOtherService/orderOther/query | **POST** | query orders |
| /api/v1/orderOtherService/orderOther/refresh | **POST** | query order for refresh |
| /api/v1/orderOtherService/orderOther/{travelDate}/{trainNumber} | GET | query sold tickets |
| /api/v1/orderOtherService/orderOther/price/{orderId} | GET | get order price by orderId |
| /api/v1/orderOtherService/orderOther/orderPay/{orderId} | GET | pay a order |
| /api/v1/orderOtherService/orderOther/{orderId} | GET | get order by id |
| /api/v1/orderOtherService/orderOther/status/{orderId}/{status} | **GET** | Modify order |
| /api/v1/orderOtherService/orderOther/security/{checkDate}/{accountId} | GET | check order's security |
| /api/v1/orderOtherService/orderOther | PUT | modify orderothers |
| /api/v1/orderOtherService/orderOther/admin |  PUT | update order |
| /api/v1/orderOtherService/orderOther/{orderId} | DELETE | delete order |
| /api/v1/orderOtherService/orderOther | GET | find all order |

 

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-station-service | /api/v1/stationservice/stations/namelist | POST | query station name list 


### ts-order-service

Summary:
This service provide API to manage order(means order which train's ID **IS** start with G 

**(There's some RESTful problems in this service for now.)**

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/orderservice/order/tickets | **POST** | get ticket list by date and trip id |
| /api/v1/orderservice/order/ | POST | create order |
| /api/v1/orderservice/order/admin | POST |admin create new order |
| /api/v1/orderservice/order/query | **POST** | query orders |
| /api/v1/orderservice/order/refresh | **POST** | query order for refresh |
| /api/v1/orderservice/order/{travelDate}/{trainNumber} | GET | query sold tickets |
| /api/v1/orderservice/order/price/{orderId} | GET | get order price by orderId |
| /api/v1/orderservice/order/orderPay/{orderId} | GET | pay a order |
| /api/v1/orderservice/order/{orderId} | GET | get order by id |
| /api/v1/orderservice/order/status/{orderId}/{status} | **GET** | Modify order |
| /api/v1/orderservice/order/security/{checkDate}/{accountId} | GET | check order's security |
| /api/v1/orderservice/order | PUT | modify orders |
| /api/v1/orderservice/order/admin |  PUT | update order |
| /api/v1/orderservice/order/{orderId} | DELETE | delete order |
| /api/v1/orderservice/order | GET | find all order |

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-station-service | /api/v1/stationservice/stations/namelist | POST | query station name list 




### ts-payment-service
Summary:
This service provide API to create payment, top up money and query payment

***
Main APIs:

| URI  |  Http Method | Description | 
| --- | --- | --- |
| /api/v1/paymentservice/payment | POST | create a payment 
| /api/v1/paymentservice/payment/money | POST | top up money 
| /api/v1/paymentservice/payment| GET | query payment 

***

Main Invocations:

**This service CRUD informations via database, no other Invocations.**



### ts-preserve-other-service

Summary:
This service provide API to preserve other train's ticket.

***
Main APIs:

| URI                                        | Http Method | Description     |
| ------------------------------------------ | ----------- | --------------- |
| /api/v1/preserveotherservice/preserveOther | POST        | preserve ticket |

***

Main Invocations:

| Service                 | URI                                                          | Method | Description                  |
| ----------------------- | ------------------------------------------------------------ | ------ | ---------------------------- |
| ts-ticketinfo-service   | /api/v1/ticketinfoservice/ticketinfo                         | POST   | query ticket info            |
| ts-seat-service         | /api/v1/seatservice/seats                                    | POST   | dispatch seat for this order |
| ts-user-service         | /api/v1/userservice/users/id/ + accountId                    | GET    | get account info             |
| ts-assurance-service    | /api/v1/assuranceservice/assurances/ + assuranceType + / + orderId | GET    | get assurance result         |
| ts-station-service      | /api/v1/stationservice/stations/id/ + stationName            | GET    | query station id             |
| ts-security-service     | /api/v1/securityservice/securityConfigs/ + accountId         | GET    | check security config        |
| ts-travel2-service      | /api/v1/travel2service/trip_detail                           | POST   | get trip detail result       |
| ts-contacts-service     | /api/v1/contactservice/contacts/+contactsId                  | GET    | get contacts by Id           |
| ts-order-other-service  | /api/v1/orderOtherService/orderOther                         | POST   | create order                 |
| ts-food-service         | /api/v1/foodservice/orders                                   | POST   | order food                   |
| ts-consign-service      | /api/v1/consignservice/consigns                              | POST   | create consign entity        |





### ts-preserve-service

Summary:
This service provide API to preserve train's ticket.

***
Main APIs:

| URI                              | Http Method | Description     |
| -------------------------------- | ----------- | --------------- |
| /api/v1/preserveservice/preserve | POST        | preserve ticket |

***

Main Invocations:

| Service                 | URI                                                          | Method | Description                  |
| ----------------------- | ------------------------------------------------------------ | ------ | ---------------------------- |
| ts-ticketinfo-service   | /api/v1/ticketinfoservice/ticketinfo                         | POST   | query ticket info            |
| ts-seat-service         | /api/v1/seatservice/seats                                    | POST   | dispatch seat for this order |
| ts-user-service         | /api/v1/userservice/users/id/ + accountId                    | GET    | get account info             |
| ts-assurance-service    | /api/v1/assuranceservice/assurances/ + assuranceType + / + orderId | GET    | get assurance result         |
| ts-station-service      | /api/v1/stationservice/stations/id/ + stationName            | GET    | query station id             |
| ts-security-service     | /api/v1/securityservice/securityConfigs/ + accountId         | GET    | check security config        |
| ts-travel-service       | /api/v1/travelservice/trip_detail                            | POST   | get trip detail result       |
| ts-contacts-service     | /api/v1/contactservice/contacts/+contactsId                  | GET    | get contacts by Id           |
| ts-order-service        | /api/v1/orderService/order                                   | POST   | create order                 |
| ts-food-service         | /api/v1/foodservice/orders                                   | POST   | order food                   |
| ts-consign-service      | /api/v1/consignservice/consigns                              | POST   | create consign entity        |


### ts-price-service

Summary:
This service provide API to calculate tickets' price and manage price configs.

***
Main APIs:

| URI  |  Http Method | Description | 
| --- | --- | --- |
| /api/v1/priceservice/prices/{routeId}/{trainType} | GET | get price by routeid and train type. 
| /api/v1/priceservice/prices | GET | get all price config |
| /api/v1/priceservice/prices | POST | create price config | 
| /api/v1/priceservice/prices | DELETE | delete price config | 
| /api/v1/priceservice/prices | PUT | update price config |

***

Main Invocations:

**This service CRUD informations via database, no other Invocations.**



### ts-rebook-service
Summary:
Train ticket rescheduling API and differential payment API are provided in this service.

***
Main APIs:

| URI                                     | Http Method | Description                  |
| --------------------------------------- | ----------- | ---------------------------- |
| /api/v1/rebookservice/rebook            | POST        | endorse train ticket         |
| /api/v1/rebookservice/rebook/difference | POST        | pay for the price difference |

***

Main Invocations:

| Service                   | URI                                                  | Method | Description                                 |
| ------------------------- | ---------------------------------------------------- | ------ | ------------------------------------------- |
| ts-ticketinfo-service     | /api/v1/ticketinfoservice/ticketinfo                 | POST   | query ticket info                           |
| ts-seat-service           | /api/v1/seatservice/seats                            | POST   | dispatch seat for this order                |
| ts-train-service          | /api/v1/trainservice/trains/byName/ + trainTypeName  | GET    | get train type, as param for dispatch seat |
| ts-route-service          | /api/v1/routeservice/routes/ + routeId               | GET    | query stations, as param for dispatch seat |
| ts-security-service       | /api/v1/securityservice/securityConfigs/ + accountId | GET    | check security config                       |
| ts-travel-service         | /api/v1/travelservice/trip_detail                    | POST   | get trip detail result                      |
| ts-travel2-service        | /api/v1/travel2service/trip_detail                   | POST   | get trip detail result                      |
| ts-order-service          | /api/v1/orderService/order                           | POST   | create order                                |
| ts-order-other-service    | /api/v1/orderOtherService/orderOther                 | POST   | create order                                |
| ts-inside-payment-service | /api/v1/inside_pay_service/inside_payment/difference | POST   | internal payment                            |




### ts-route-plan-service
Summary:
This service provide APIs to get cheapest Route, quickest Route and min Stop Stations Route.

***
Main APIs:

| URI                                                | Http Method | Description                  |
| -------------------------------------------------- | ----------- | ---------------------------- |
| /api/v1/routeplanservice/routePlan/cheapestRoute   | POST        | get routes in cheapest order |
| /api/v1/routeplanservice/routePlan/quickestRoute   | POST        | get routes in quickest order |
| /api/v1/routeplanservice/routePlan/minStopStations | POST        | get routes in minStops Order |

***

Main Invocations:

| Service            | URI                                             | Method | Description               |
| ------------------ | ----------------------------------------------- | ------ | ------------------------- |
| ts-travel-service  | /api/v1/travelservice/trip_detail               | POST   | get trip detail           |
| ts-station-service | /api/v1/stationservice/stations/id/+stationName | GET    | get stationId by name     |
| ts-route-service   | /api/v1/routeservice/routes/ + routeId          | GET    | get route info by routeId |
| ts-travel-service  | /api/v1/travelservice/trips/left                | POST   | get trip information      |
| ts-travel2-service | /api/v1/travel2service/trips/left               | POST   | get trip information      |
| ts-travel-service  | /api/v1/travelservice/routes/ + tripId          | GET    | get station list          |
| ts-travel-service  | /api/v1/travelservice/trips/routes              | GET    | get trips by routeId      |
| ts-travel2-service  | /api/v1/travel2service/trips/routes              | GET    | get trips by routeId      |


### ts-route-service

Summary:
This service provide APIs to CRUD route informations.

***
Main APIs:

| URI                                                | Http Method | Description                              |
| -------------------------------------------------- | ----------- | ---------------------------------------- |
| /api/v1/routeservice/routes                        | POST        | create and modify route info             |
| /api/v1/routeservice/routes/{routeId}              | DELETE      | delete route info                        |
| /api/v1/routeservice/routes/{routeId}              | GET         | GET route info                           |
| /api/v1/routeservice/routes                        | GET         | GET all routes' info                     |
| /api/v1/routeservice/routes/{startId}/{terminalId} | GET         | get route by start and terminate station |

***

Main Invocations:

**This service CRUD informations via database, no other Invocations.**



### ts-seat-service
Summary:
This service provide APIs to allocate seats for users and query left tickets in an interval.



***
Main APIs:

| URI                                    | Http Method | Description                     |
| -------------------------------------- | ----------- | ------------------------------- |
| /api/v1/seatservice/seats              | POST        | allocate seats for users        |
| /api/v1/seatservice/seats/left_tickets | POST        | get left tickets in an interval |



***

Main Invocations:

| Service                | URI                                              | Method | Description                     |
| ---------------------- | ------------------------------------------------ | ------ | ------------------------------- |
| ts-travel-service      | /api/v1/travelservice/routes/+trainNumber        | GET    | get all the stops for the train |
| ts-order-service       | /api/v1/orderservice/order/tickets               | POST   | get left tickets                |
| ts-order-other-service | /api/v1/orderOtherService/orderOther/tickets     | POST   | get left tickets                |
| ts-travel-service      | /api/v1/travelservice/train_types/ + trainNumber | GET    | get train's type                |
| ts-config-service      | /api/v1/configservice/configs/+configName        | GET    | get config file                 |




### ts-security-service

Summary:
This service provide APIs to manage security config files, includes add, delete, query, modify config file and check account id.

***
Main APIs:

| URI                                                 | Http Method | Description                |
| --------------------------------------------------- | ----------- | -------------------------- |
| /api/v1/securityservice/securityConfigs             | GET         | get all security config    |
| /api/v1/securityservice/securityConfigs             | POST        | create new security config |
| /api/v1/securityservice/securityConfigs             | PUT         | modify security config     |
| /api/v1/securityservice/securityConfigs/{id}        | DELETE      | delete one config          |
| /api/v1/securityservice/securityConfigs/{accountId} | GET         | check account id           |

***

Main Invocations:

| Service                | URI                                                          | Method | Description                   |
| ---------------------- | ------------------------------------------------------------ | ------ | ----------------------------- |
| ts-order-service       | /api/v1/orderservice/order/security/ + checkDate + / + accountId | GET    | get security order info       |
| ts-order-other-service | /api/v1/orderOtherService/orderOther/security/ + checkDate + / + accountId | GET    | get security other order info |



### ts-station-service

Summary:
This service provide APIs to CRUD station information, and it also provide APIs to get station name by id and get station id by name.

***
Main APIs:

| URI  |  Http Method | Description | 
| --- | --- | --- |
| /api/v1/stationservice/stations | GET | get stations 
| /api/v1/stationservice/stations | POST | create new station 
| /api/v1/stationservice/stations | PUT | update station information 
| /api/v1/stationservice/stations/{stationsId} | DELETE | delete station 
| /api/v1/stationservice/stations/id/{stationNameForId} | GET | query station id by stationName 
| /api/v1/stationservice/stations/idlist | POST | get id list by name list | 
| /api/v1/stationservice/stations/name/{stationIdForName} | GET | get stationName by id |
| /api/v1/stationservice/stations/namelist | POST | get id list by name list |

***

Main Invocations:

**This service CRUD informations via database, no other Invocations.**




### ts-ticket-office-service

Summary:
This service provide APIs to get ticket office information and manage ticket office information.



***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /getRegionList | GET | get region which provide ticket office |
| /getAll | GET | get all ticket office info |
| /getSpecificOffices | POST | get specific ticket office |
| /addOffice | POST | add new ticket office |
| /deleteOffice | DELETE | delete ticket office |
| /updateOffice | **POST** | update ticket office |



***

Main Invocations:

**This service CRUD informations via database, no other Invocations.**




### ts-ticketinfo-service

Summary:

This service provide APIs to get travel information and query stationId.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/ticketinfoservice/ticketinfo | POST | query travel info |
| /api/v1/ticketinfoservice/ticketinfo/{name} | GET | query stationId by name |

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-basic-service | /api/v1/basicservice/basic/travel | POST | query basic travel information |
| ts-basic-service | /api/v1/basicservice/basic/" + name | GET | query stationId by name |



### ts-train-service

Summary:
This service provide APIs to manage train information, includes create train entity, update train information, get train's information and delete trains information 

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/trainservice/trains | POST | create trains entity |
| /api/v1/trainservice/trains | PUT | update trains information |
| /api/v1/trainservice/trains/{id} | GET | get train's info by id |
| /api/v1/trainservice/trains/{id} | DELETE | delete train's info by id |
| /api/v1/trainservice/trains | GET | query train types |
| /api/v1/trainservice/trains/byName/{name}| GET | get train types by name|

***

Main Invocations:

**This service CRUD informations via database, no other Invocations.**


### ts-travel-plan-service

Summary:
This service provide APIs to get travel plan, includes cheapest travel plan, quickest travel plan, min station travel plan.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| api/v1/travelplanservice/travelPlan/transferResult | POST | get transfer search result |
| api/v1/travelplanservice/travelPlan/cheapest | POST | get cheapest travel plan |
| api/v1/travelplanservice/travelPlan/quickest | POST | get quickest travel plan |
| api/v1/travelplanservice/travelPlan/minStation | POST | get min station amount travel plan

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-seat-service | /api/v1/seatservice/seats/left_tickets | POST | get left tickets |
| ts-route-plan-service | /api/v1/routeplanservice/routePlan/cheapestRoute | POST | get cheapest route |
| ts-route-plan-service | /api/v1/routeplanservice/routePlan/quickestRoute | POST | get quickest route |
| ts-route-plan-service | /api/v1/routeplanservice/routePlan/minStopStations | POST | get min stop stations route |
| ts-travel-service | /api/v1/travelservice/trips/left | POST | get high speed trip |
| ts-travel2-service | /api/v1/travel2service/trips/left | POST | get normal train's trip |
| ts-ticketinfo-service | /api/v1/ticketinfoservice/ticketinfo/+stationName | GET | query station id by Name |
| ts-station-service | /api/v1/stationservice/stations/namelist | POST | get station name list by station Id list |



### ts-travel-service

Summary:
This service provide APIs to manage high speed train's trip.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/travelservice/train_types/{tripId} | GET | get train type by tripId |
| /api/v1/travelservice/routes/{tripId} | GET | get route by tripId |
| /api/v1/travelservice/trips/routes | POST | get trip by routeId |
| /api/v1/travelservice/trips | POST | create trip |
| /api/v1/travelservice/trips/{tripId} | GET | get trip information by tripId |
| /api/v1/travelservice/trips | PUT | update trip |
| /api/v1/travelservice/trips/{tripId} | DELETE | delete a trip |
| /api/v1/travelservice/trips/left | POST | get left trip tickets |
| /api/v1/travelservice/trip_detail | POST | get trip info with left tickets |
| /api/v1/travelservice/trips | GET | query all trips |
| /api/v1/travelservice/admin_trip | GET | query all trip as an administrator |

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-basic-service | /api/v1/basicservice/basic/travel | POST | query basic travel information |
| ts-train-service | /api/v1/trainservice/trains/byName/ | GET | get trainType by train type name| |
| ts-route-service | /api/v1/routeservice/routes/ + routeId | GET | get route information by routeId |
| ts-seat-service | /api/v1/seatservice/seats/left_tickets | POST | get left tickets |


### ts-travel2-service

Summary:
This service provide APIs to manage normal train's trip.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/travel2service/train_types/{tripId} | GET | get train type by tripId |
| /api/v1/travel2service/routes/{tripId} | GET | get route by tripId |
| /api/v1/travel2service/trips/routes | POST | get trip by routeId |
| /api/v1/travel2service/trips | POST | create trip |
| /api/v1/travel2service/trips/{tripId} | GET | get trip information by tripId |
| /api/v1/travel2service/trips | PUT | update trip |
| /api/v1/travel2service/trips/{tripId} | DELETE | delete a trip |
| /api/v1/travel2service/trips/left | POST | get left trip tickets |
| /api/v1/travel2service/trip_detail | POST | get trip info with left tickets |
| /api/v1/travel2service/trips | GET | query all trips |
| /api/v1/travel2service/admin_trip | GET | query all trip as an administrator |

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-basic-service | /api/v1/basicservice/basic/travel | POST | query basic travel information |
| ts-train-service | /api/v1/trainservice/trains/byName/ | GET | get trainType by train type name| |
| ts-route-service | /api/v1/routeservice/routes/ + routeId | GET | get route information by routeId |
| ts-seat-service | /api/v1/seatservice/seats/left_tickets | POST | get left tickets |



### ts-ui-dashboard
Summary:
This repository provide all the UI interface to interact with the system.



### ts-ui-test
Summary: 
This repository provide all tests to test UI interface.



### ts-user-service

Summary:
This service provide APIs to manage user information, includes register user, get user by username, get user by userId, and allow administrator to delete a user.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/userservice/users | GET | get all users |
| /api/v1/userservice/users/{userName} | GET | get user by username |
| /api/v1/userservice/users/id/{userId} | GET | get user by id |
| /api/v1/userservice/users/register | POST | register user |
| /api/v1/userservice/users/{userId} | DELETE | delete user by id |



***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-auth-service | /api/v1/auth | POST | create default user |


### ts-verification-code-service

Summary:

This service provide APIs to generate verification code pictures and verify code which user send.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/verifycode/generate | GET | generate verification code |
| /api/v1/verifycode/verify/{verifyCode} | GET | check verify code which user send |

***

Main Invocations:

No other Invocations.


### ts-voucher-service

Summary:
This service provide APIs to generate the reimbursement voucher based on the order id.

***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /getVoucher | POST | generate the reimbursement voucher based on the order id |

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-order-other-service | /api/v1/orderOtherService/orderOther/ + orderId | GET | query order information by orderId |
| ts-order-service | /api/v1/orderservice/order/+orderId | GET | query high speed order information by orderId |

### ts-wait-order-service

Summary: 
This service provide APIs to manage waitlist orders.

***
Main APIs:
| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/waitorderservice/order| POST | create a waitlist order |
| /api/v1/waitorderservice//waitlistorders | GET | get all waitlist orders | 

***
Main Invocations:

No other Invocations.


### ts-train-food-service

Summary:
This service provide APIs to CRUD train food.



***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/trainfoodservice/trainfoods | GET| get all train food |
| /api/v1/trainfoodservice/trainfoods/{tripId} | GET| get train food list by trip id |

***

Main Invocations:

No other Invocations.

### ts-station-food-service

Summary:
This service provide APIs to CRUD station food.



***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/stationfoodservice/stationfoodstores | GET| get all station food stores |
| /api/v1/stationfoodservice/stationfoodstores/{stationId} | GET| get station food stores by StationName|
| /api/v1/stationfoodservice/stationfoodstores | POST| get station food stores by StationNames|
| /api/v1/stationfoodservice/stationfoodstores/bystoreid/{stationFoodStoreId} | GET| get a station food store by stationFoodStoreId|

***

Main Invocations:

No other Invocations.


### ts-food-delivery-service

Summary:
This service provide APIs to CRUD food delivery orders.



***
Main APIs:

| URI  |  Http Method | Description |
| --- | --- | --- |
| /api/v1/fooddeliveryservice/orders | POST| create a food delivery order |
| /api/v1/fooddeliveryservice/orders/d/{orderId} | DELETE| delete a food delivery order |
| /api/v1/fooddeliveryservice/orders/{orderId} | GET| get a food delivery order |
| /api/v1/fooddeliveryservice/orders/all | GET| get all food delivery orders |
| /api/v1/fooddeliveryservice/orders/store/{storeId} | GET | get food delivery orders by storeId|
| /api/v1/fooddeliveryservice/orders/tripid | PUT| update food delivery orders' trip id |
| /api/v1/fooddeliveryservice/orders/seatno | PUT| update food delivery orders' seat number|
| /api/v1/fooddeliveryservice/orders/dtime | PUT| update food delivery orders' delivery time|

***

Main Invocations:

| Service | URI | Method | Description |
| --- | --- | --- | --- |
| ts-station-food-service | /api/v1/stationfoodservice/stationfoodstores/bystoreid/ + stationFoodStoreId| GET | get station food store info by id|
