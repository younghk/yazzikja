package sogong.korea.yazzikja.model

data class ImageModel ( var explain: String? = null,
                        var imageUrl : String? = null,
                        var uid : String? = null, //pk
                        var userId : String? = null, //email address.
                        var timestamp : Long? = null,
                        var favoriteCount : Int = 0, //count like
                        var favorites : MutableMap<String,Boolean> = HashMap() // count overlap check
                        ) {
        data class Comment(var uid : String? = null,
                           var userId : String? = null,
                           var comment : String? = null,
                           var timestamp: Long? = null
                            )
}

