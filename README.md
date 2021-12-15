# Large Scale Project
## _SecondChance_

![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white) 

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white) 

![MongoDB](https://img.shields.io/badge/MongoDB-%234ea94b.svg?style=for-the-badge&logo=mongodb&logoColor=white)
![Neo4J](https://img.shields.io/badge/Neo4j-008CC1?style=for-the-badge&logo=neo4j&logoColor=white) 

![Vue.js](https://img.shields.io/badge/vuejs-%2335495e.svg?style=for-the-badge&logo=vuedotjs&logoColor=%234FC08D)

![LaTeX](https://img.shields.io/badge/latex-%23008080.svg?style=for-the-badge&logo=latex&logoColor=white)

VintedUnipi is an e-commerce that offers the possibility of selling and buying vinted clothes.
Every user can add his or her products on the profile or buy some clothes that are selled by people in the nearby or through direct searches. 

## Features

### Admin

- Register/Login/Logout

- Delete post

- Delete review

- Suspend user

- Generate stats

- Generate codes for account balances

### User

- Register, login and logout

- Browse the feed

- Create, update or delete a post to sell a post

- Find posts by filters

- Find local sellers

- Buy an item

- Write a review after a purchase

- Like/unlike a post

- Follow/unfollow a user

 ### Queries
 
 #### MongoDB
 
- View local posts

- Search by location

- Search by category

- Search by brand

- Search by most viewed posts

- View top 50 liked local posts 

- View top 50 viewed local posts

- View top 50 rated user for current user country

- View top 50 liked posts for the selected category

- View the best/worst rated reviews of a user
 
 #### Neo4J
 
- Suggest new sellers based on purchases, location, similar search parameters, likes

- Suggest new posts based on purchases, location, similar search parameters, likes

- Suggest cheaper items

### Details

There is not a cart: a user can buy one item at a time clicking on the "shop" button.
The part regarding users correlation is managed with Neo4j.

A review is done a rating (1-5 stars) and a comment regarding the product and is associated to the user.
Every user has a personal wallet that can recharge inserting a code in a specific field.
When a purchase is copmuted the amount is decreased.

Every post has a "sold" field and an ID: when a user purchases an item the ID relative to the post is memorized in an array of the user collection.

Suggestions are based on different infomations like similar purchases, location, search parameters, likes and feed.

## Link

[Dataset1] [Dataset2] containing items.

[Dataset3] containing reviews.

## Actors

Actors  | Role
------------- | -------------
Normal User  | Can buy or sell
Admin | Can delete items, posts and every inappropriate content. Can suspend a user, generate statistics and codes.

## License

MIT

**Free Software, Hell Yeah!**

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)

   [Dataset1]: <https://www.kaggle.com/agrigorev/clothing-dataset-full>
      
   [Dataset2]: <https://data.world/jfreex/products-catalog-from-newchiccom>
   
   [Dataset3]: <https://www.kaggle.com/asmaoueslati/womensclothingecommerce>

   
 
