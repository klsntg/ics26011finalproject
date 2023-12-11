package com.example.ics26011finalproject

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.Serializable


data class User(
    val email: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String
)
data class Category(
    val id: Int,
    val category: String
)
data class Details(
    val id: Int,
    val title: String,
    val author: String,
    val categoryId: Int,
    val imageSource: String,
    val rating: String,
    val price: String,
    val description: String,
    val addToLibrary: Boolean,
    val addToFave: Boolean
) : Serializable

class DatabaseHandler (context: Context) : SQLiteOpenHelper (context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "UserDatabase.db"

        private const val TABLE_USERS = "users"
        private const val KEY_EMAIL = "email_address"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"

        private const val TABLE_CATEGORIES = "categories"
        private const val KEY_CATEGORY_ID = "id"
        private const val KEY_CATEGORY_NAME = "category"

        private const val TABLE_DETAILS = "details"
        private const val KEY_DETAILS_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_AUTHOR = "author"
        private const val KEY_CATEGORY_ID_FK = "category_id_fk"
        private const val KEY_IMAGE_SOURCE = "image_source"
        private const val KEY_RATING = "rating"
        private const val KEY_PRICE = "price"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_ADD_TO_LIBRARY = "add_to_library"
        private const val KEY_ADD_TO_FAVE = "add_to_fave"

        private const val TABLE_LIBRARY = "library"
        private const val KEY_LIBRARY_ID = "library_id"
        private const val KEY_USER_EMAIL_FK = "user_email_fk"
        private const val KEY_BOOK_ID_FK = "book_id_fk"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_USERS_TABLE = ("CREATE TABLE $TABLE_USERS (" +
                "$KEY_EMAIL TEXT PRIMARY KEY," +
                "$KEY_FIRST_NAME TEXT," +
                "$KEY_LAST_NAME TEXT," +
                "$KEY_USERNAME TEXT," +
                "$KEY_PASSWORD TEXT)")

        val CREATE_CATEGORIES_TABLE = ("CREATE TABLE $TABLE_CATEGORIES (" +
                "$KEY_CATEGORY_ID INTEGER PRIMARY KEY," +
                "$KEY_CATEGORY_NAME TEXT)")

        val CREATE_DETAILS_TABLE = ("CREATE TABLE $TABLE_DETAILS (" +
                "$KEY_DETAILS_ID INTEGER PRIMARY KEY," +
                "$KEY_TITLE TEXT," +
                "$KEY_AUTHOR TEXT," +
                "$KEY_CATEGORY_ID_FK INTEGER," +
                "$KEY_IMAGE_SOURCE TEXT," +
                "$KEY_RATING TEXT," +
                "$KEY_PRICE TEXT," +
                "$KEY_DESCRIPTION TEXT," +
                "$KEY_ADD_TO_LIBRARY INTEGER, " +
                "$KEY_ADD_TO_FAVE INTEGER, " +
                "FOREIGN KEY($KEY_CATEGORY_ID_FK) REFERENCES $TABLE_CATEGORIES($KEY_CATEGORY_ID))")

        val CREATE_LIBRARY_TABLE = ("CREATE TABLE $TABLE_LIBRARY (" +
                "$KEY_LIBRARY_ID INTEGER PRIMARY KEY," +
                "$KEY_USER_EMAIL_FK TEXT," +
                "$KEY_BOOK_ID_FK INTEGER," +
                "FOREIGN KEY($KEY_USER_EMAIL_FK) REFERENCES $TABLE_USERS($KEY_EMAIL)," +
                "FOREIGN KEY($KEY_BOOK_ID_FK) REFERENCES $TABLE_DETAILS($KEY_DETAILS_ID))")

        db?.execSQL(CREATE_USERS_TABLE)
        db?.execSQL(CREATE_CATEGORIES_TABLE)
        db?.execSQL(CREATE_DETAILS_TABLE)
        db?.execSQL(CREATE_LIBRARY_TABLE)

        insertInitialCategories(db)
        insertInitialDetails(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }

    fun registerUser(email: String, fname: String, lname: String, username: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_EMAIL, email)
            put(KEY_FIRST_NAME, fname)
            put(KEY_LAST_NAME, lname)
            put(KEY_USERNAME, username)
            put(KEY_PASSWORD, password)
        }

        val success = db.insert(TABLE_USERS, null, values)
        db.close()

        return success
    }

    fun loginUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val selection = "$KEY_USERNAME = ? AND $KEY_PASSWORD = ?"
        val selectionArgs = arrayOf(username, password)
        val cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null)

        val userExists = cursor.moveToFirst() && cursor.count > 0
        cursor.close()

        return userExists
    }

    fun getUserInfo(username: String): User? {
        val db = this.readableDatabase
        val selection = "$KEY_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val cursor: Cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null)

        return cursor.use {
            if (it.moveToFirst()) {
                val emailIndex = it.getColumnIndex(KEY_EMAIL)
                val firstNameIndex = it.getColumnIndex(KEY_FIRST_NAME)
                val lastNameIndex = it.getColumnIndex(KEY_LAST_NAME)
                val usernameIndex = it.getColumnIndex(KEY_USERNAME)
                val passwordIndex = it.getColumnIndex(KEY_PASSWORD)

                if (emailIndex != -1 && firstNameIndex != -1 && lastNameIndex != -1 &&
                    usernameIndex != -1 && passwordIndex != -1) {
                    return User(
                        it.getString(emailIndex),
                        it.getString(firstNameIndex),
                        it.getString(lastNameIndex),
                        it.getString(usernameIndex),
                        it.getString(passwordIndex)
                    )
                }
                else {
                    null
                }
            }
            else {
                null
            }
        }
    }
    private fun insertInitialCategories(db: SQLiteDatabase?) {
        val categories = listOf("Non-Fiction","Classics","Fantasy","Young Adult","Crime","Horror","Sci-Fi","Drama")

        categories.forEach { category ->
            val values = ContentValues().apply {
                put(KEY_CATEGORY_NAME, category)
            }

            db?.insert(TABLE_CATEGORIES, null, values)
        }
    }
    private fun insertInitialDetails(db: SQLiteDatabase?) {

        val book1 = ContentValues().apply {
            put(KEY_TITLE, "A Brief History of Humankind")
            put(KEY_AUTHOR, "Yuval Noah Harari")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Non-Fiction", db))
            put(KEY_RATING, "4.4/5")
            put(KEY_PRICE, "PHP 1,000")
            put(KEY_DESCRIPTION,"Sapiens: A Brief History of Humankind\" by Yuval Noah Harari offers a sweeping overview of human history, exploring the evolution of Homo sapiens from ancient times to the present. Harari covers vital milestones, including the Cognitive and Agricultural Revolutions, the formation of empires, and the impact of scientific advancements. The book prompts readers to reconsider established narratives, examining the interplay of biology and culture in shaping human societies. Through engaging storytelling, Harari presents a thought-provoking reflection on our species' past, present, and potential future.")
            put(KEY_IMAGE_SOURCE, "drawable/book1")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book1)

        val book2 = ContentValues().apply {
            put(KEY_TITLE, "The Picture of Dorian Gray")
            put(KEY_AUTHOR, "Oscar Wilde")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Classics", db))
            put(KEY_RATING, "4.11/5")
            put(KEY_PRICE, "PHP 1,300")
            put(KEY_DESCRIPTION,"Oscar Wilde’s only novel is the dreamlike story of a young man who sells his soul for eternal youth and beauty. Wilde forged a devastating portrait of the effects of evil and debauchery on a young aesthete in late-19th-century England in this celebrated work. Combining elements of the Gothic horror novel and decadent French fiction, the book centers on a striking premise: As Dorian Gray sinks into a life of crime and gross sensuality, his body retains perfect youth and vigor while his recently painted portrait grows day by day into a hideous record of evil, which he must keep hidden from the world. This mesmerizing tale of horror and suspense has been popular for over a century. It ranks as one of Wilde's most important creations and among the classic achievements of its kind.")
            put(KEY_IMAGE_SOURCE, "drawable/book2")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book2)

        val book3 = ContentValues().apply {
            put(KEY_TITLE, "The Name of the Wind")
            put(KEY_AUTHOR, "Patrick Rothfuss")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Fantasy", db))
            put(KEY_RATING, "4.55/5")
            put(KEY_PRICE, "PHP 1,055")
            put(KEY_DESCRIPTION,"\"The Name of the Wind\" follows the tale of Kvothe, a gifted young musician and magician. The story, narrated by Kvothe, recounts his journey from a gifted child in a traveling troupe to a powerful wizard, unraveling mysteries and facing formidable challenges. Rothfuss weaves a rich and immersive narrative, blending magic, music, and the intricate tapestry of Kvothe's life in a captivating fantasy world.")
            put(KEY_IMAGE_SOURCE, "drawable/book3")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book3)

        val book4 = ContentValues().apply {
            put(KEY_TITLE, "The Hunger Games")
            put(KEY_AUTHOR, "Suzanne Collins")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Young Adult", db))
            put(KEY_RATING, "4.34/5")
            put(KEY_PRICE, "PHP 600")
            put(KEY_DESCRIPTION,"In a dystopian future, Katniss Everdeen volunteers to take her sister's place in the annual Hunger Games, a televised fight to the death. Suzanne Collins crafts a thrilling narrative that explores survival, sacrifice, and rebellion themes.")
            put(KEY_IMAGE_SOURCE, "drawable/book4")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book4)

        val book5 = ContentValues().apply {
            put(KEY_TITLE, "The Girl with the Dragon Tattoo")
            put(KEY_AUTHOR, "Stieg Larsson")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Crime", db))
            put(KEY_RATING, "4.14/5")
            put(KEY_PRICE, "PHP 300")
            put(KEY_DESCRIPTION,"Mikael Blomkvist, a journalist, teams up with a brilliant hacker, Lisbeth Salander, to solve a decades-old disappearance in Sweden. Stieg Larsson's gripping novel combines crime, intrigue, and a complex investigation.")
            put(KEY_IMAGE_SOURCE, "drawable/book5")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book5)

        val book6 = ContentValues().apply {
            put(KEY_TITLE, "The Shining")
            put(KEY_AUTHOR, "Stephen King")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Horror", db))
            put(KEY_RATING, "4.21/5")
            put(KEY_PRICE, "PHP 400")
            put(KEY_DESCRIPTION,"Stephen King's \"The Shining\" takes readers to the eerie Overlook Hotel, where the Torrance family faces supernatural forces. As the hotel exerts its malevolent influence, the story becomes a chilling exploration of isolation and madness.")
            put(KEY_IMAGE_SOURCE, "drawable/book6")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book6)

        val book7 = ContentValues().apply {
            put(KEY_TITLE, "Dune")
            put(KEY_AUTHOR, "Frank Herbert")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Sci-Fi", db))
            put(KEY_RATING, "4.23/5")
            put(KEY_PRICE, "PHP 700")
            put(KEY_DESCRIPTION,"\"Dune\" immerses readers in a universe where noble houses vie for control of the desert planet Arrakis, the only source of the spice melange. Paul Atreides, the heir to House Atreides, becomes entangled in political intrigue, prophetic visions, and the ecological complexities of Arrakis. Herbert weaves a tapestry of power struggles, mysticism, and environmentalism, exploring themes of identity and leadership. The narrative's depth extends to intricate world-building, with a focus on the Fremen, the indigenous people of Arrakis, and the transformative journey of Paul, whose destiny is intertwined with the planet and its precious spice.")
            put(KEY_IMAGE_SOURCE, "drawable/book7")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book7)

        val book8 = ContentValues().apply {
            put(KEY_TITLE, "The Kite Runner")
            put(KEY_AUTHOR, "Khaled Hosseini")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Drama", db))
            put(KEY_RATING, "4.29/5")
            put(KEY_PRICE, "PHP 400")
            put(KEY_DESCRIPTION,"Khaled Hosseini's \"The Kite Runner\" explores friendship, betrayal, and redemption in war-torn Afghanistan. The novel follows the intertwined lives of Amir and Hassan against a backdrop of historical upheaval.")
            put(KEY_IMAGE_SOURCE, "drawable/book8")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book8)

        val book9 = ContentValues().apply {
            put(KEY_TITLE, "The Power of Habit")
            put(KEY_AUTHOR, "Charles Duhigg")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Non-Fiction", db))
            put(KEY_RATING, "4.7/5")
            put(KEY_PRICE, "PHP 800")
            put(KEY_DESCRIPTION,"The Power of Habit\" by Charles Duhigg is an illuminating exploration into the science of habits. Duhigg delves into the neurological patterns that shape our routines, providing insights into how habits work and how they can be transformed. With compelling real-life stories and insightful analysis, the book offers practical advice for harnessing the power of habit to achieve positive change.")
            put(KEY_IMAGE_SOURCE, "drawable/book9")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book9)

        val book10 = ContentValues().apply {
            put(KEY_TITLE, "Pride and Prejudice")
            put(KEY_AUTHOR, "Jane Austen")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Classics", db))
            put(KEY_RATING, "4.8/5")
            put(KEY_PRICE, "PHP 600")
            put(KEY_DESCRIPTION,"\"Pride and Prejudice\" by Jane Austen is a timeless classic that explores themes of love, class, and societal expectations. Set in the Regency era, the novel follows the headstrong Elizabeth Bennet as she navigates the challenges of love and social norms. Austen's wit and insight into human nature make this novel a captivating exploration of the complexities of relationships in a bygone era.\n")
            put(KEY_IMAGE_SOURCE, "drawable/book10")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book10)

        val book11 = ContentValues().apply {
            put(KEY_TITLE, "The Hobbit")
            put(KEY_AUTHOR, "J.R.R Tolkien")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Fantasy", db))
            put(KEY_RATING, "4.9/5")
            put(KEY_PRICE, "PHP 700")
            put(KEY_DESCRIPTION,"\"The Hobbit\" by J.R.R. Tolkien is a timeless and enchanting fantasy novel that takes readers on an epic adventure in the enchanting realm of Middle-earth. The story follows Bilbo Baggins, a content and unassuming hobbit, who is thrust into an extraordinary journey when the wizard Gandalf and a group of dwarves arrive at his door seeking assistance. Together, they set out on a perilous quest to reclaim the Lonely Mountain and its treasure guarded by the formidable dragon Smaug. Along the way, Bilbo encounters trolls, goblins, elves, and other fantastical creatures, each contributing to the richness of Tolkien's meticulously crafted world. Filled with themes of bravery, friendship, and self-discovery, \"The Hobbit\" captivates readers with its charming narrative, memorable characters, and the enduring appeal of Tolkien's masterful storytelling.The novel's enduring popularity lies in its ability to transport readers to a magical world, offering a perfect blend of adventure, humor, and heart. Bilbo's personal growth from a timid hobbit to a courageous hero, the riddles in the dark with Gollum, and the grandeur of the Battle of the Five Armies are just a few highlights that make \"The Hobbit\" a beloved classic, appealing to readers of all ages.")
            put(KEY_IMAGE_SOURCE, "drawable/book11")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book11)

        val book12 = ContentValues().apply {
            put(KEY_TITLE, "The Fault in Our Stars")
            put(KEY_AUTHOR, "John Green")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Young Adult", db))
            put(KEY_RATING, "4.5/5")
            put(KEY_PRICE, "PHP 500")
            put(KEY_DESCRIPTION,"\"The Fault in Our Stars\" by John Green is a poignant young adult novel that tells the story of Hazel Grace Lancaster, a teenager living with cancer. The novel explores themes of love, loss, and the search for meaning in the face of illness. Green's heartfelt prose and relatable characters make this book a powerful exploration of the human experience for young adult readers.")
            put(KEY_IMAGE_SOURCE, "drawable/book12")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book12)

        val book13 = ContentValues().apply {
            put(KEY_TITLE, "The Silence of the Lambs")
            put(KEY_AUTHOR, "Thomas Harris")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Crime", db))
            put(KEY_RATING, "4.9/5")
            put(KEY_PRICE, "PHP 800")
            put(KEY_DESCRIPTION,"\"The Silence of the Lambs\" by Thomas Harris is a gripping psychological thriller that introduces readers to FBI trainee Clarice Starling and the brilliant, but insane, Dr. Hannibal Lecter. Starling seeks Lecter's help in solving a case involving a serial killer, leading to a chilling cat-and-mouse game. Harris's masterful storytelling and complex characters make this novel a classic in the crime genre.")
            put(KEY_IMAGE_SOURCE, "drawable/book13")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book13)

        val book14 = ContentValues().apply {
            put(KEY_TITLE, "IT")
            put(KEY_AUTHOR, "Stephen King")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Horror", db))
            put(KEY_RATING, "4.3/5")
            put(KEY_PRICE, "PHP 800")
            put(KEY_DESCRIPTION,"\"IT\" by Stephen King is a chilling horror novel that follows a group of friends in the town of Derry as they confront a malevolent entity that takes the form of Pennywise the Dancing Clown. King weaves a tale of childhood fears, friendship, and the enduring power of evil.")
            put(KEY_IMAGE_SOURCE, "drawable/book14")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book14)

        val book15 = ContentValues().apply {
            put(KEY_TITLE, "Neuromancer")
            put(KEY_AUTHOR, "William Gibson")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Sc-fi", db))
            put(KEY_RATING, "4.2/5")
            put(KEY_PRICE, "PHP 780")
            put(KEY_DESCRIPTION, "\"Neuromancer\" by William Gibson is a groundbreaking science fiction novel that introduces readers to the concept of cyberspace. Case, a washed-up computer hacker, is hired for one last job that leads him into a world of artificial intelligence and corporate espionage. Gibson's visionary storytelling has had a profound influence on the cyberpunk genre.")
            put(KEY_IMAGE_SOURCE, "drawable/book15")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book15)

        val book16 = ContentValues().apply {
            put(KEY_TITLE, "To Kill a Mockingbird")
            put(KEY_AUTHOR, "Harper Lee")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Drama", db))
            put(KEY_RATING, "4.6/5")
            put(KEY_PRICE, "PHP 870")
            put(KEY_DESCRIPTION, "\"To Kill a Mockingbird\" by Harper Lee is a classic drama set in the racially charged American South. Through the eyes of Scout Finch, the novel explores themes of racial injustice, morality, and compassion. Lee's poignant narrative and unforgettable characters make this a timeless work.")
            put(KEY_IMAGE_SOURCE, "drawable/book16")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book16)

        val book17 = ContentValues().apply {
            put(KEY_TITLE, "Astrophysics for People in a Hurry")
            put(KEY_AUTHOR, "Neil deGrasse Tyson")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Non-Fiction", db))
            put(KEY_RATING, "4.5/5")
            put(KEY_PRICE, "PHP 750")
            put(KEY_DESCRIPTION, "\"Astrophysics for People in a Hurry\" by Neil deGrasse Tyson is a concise journey through the cosmos for those with a busy schedule. Tyson breaks down complex astrophysical concepts into bite-sized explanations, making the wonders of the universe accessible to all. Whether you're a novice or a science enthusiast, this book promises to expand your cosmic perspective in an engaging and digestible way.")
            put(KEY_IMAGE_SOURCE, "drawable/book17")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book17)

        val book18 = ContentValues().apply {
            put(KEY_TITLE, "Jane Eyre")
            put(KEY_AUTHOR, "Charlotte Brontë")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Classics", db))
            put(KEY_RATING, "4.7/5")
            put(KEY_PRICE, "PHP 650")
            put(KEY_DESCRIPTION, "\"Jane Eyre\" by Charlotte Brontë is a classic novel that tells the story of the orphaned and mistreated Jane as she rises above societal expectations...")
            put(KEY_IMAGE_SOURCE, "drawable/book18")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book18)

        val book19 = ContentValues().apply {
            put(KEY_TITLE, "A Game of Thrones")
            put(KEY_AUTHOR, "George R.R. Martin")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Fantasy", db))
            put(KEY_RATING, "4.6/5")
            put(KEY_PRICE, "PHP 780")
            put(KEY_DESCRIPTION, "\"A Game of Thrones\" by George R.R. Martin is the first book in the epic fantasy series \"A Song of Ice and Fire.\" The novel introduces readers to the complex and morally ambiguous world of Westeros...")
            put(KEY_IMAGE_SOURCE, "drawable/book19")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book19)

        val book20 = ContentValues().apply {
            put(KEY_TITLE, "To All the Boys I've Loved Before")
            put(KEY_AUTHOR, "Jenny Han")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Young Adult", db))
            put(KEY_RATING, "4.4/5")
            put(KEY_PRICE, "PHP 650")
            put(KEY_DESCRIPTION, "\"To All the Boys I've Loved Before\" by Jenny Han is a charming young adult romance that follows Lara Jean Covey as her secret love letters are accidentally sent to her past crushes. The novel explores themes of identity, family, and first love. Han's delightful storytelling has made this book a favorite among young adult readers.")
            put(KEY_IMAGE_SOURCE, "drawable/book20")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book20)


        val book21 = ContentValues().apply {
            put(KEY_TITLE, "Gone Girl")
            put(KEY_AUTHOR, "Gillian Flynn")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Crime", db))
            put(KEY_RATING, "4.2/5")
            put(KEY_PRICE, "PHP 780")
            put(KEY_DESCRIPTION,"\"Gone Girl\" by Gillian Flynn is a gripping crime thriller that explores the complexities of marriage and deception. When Amy Dunne goes missing on her fifth wedding anniversary, suspicion falls on her husband Nick. As the mystery unravels, Flynn crafts a psychological thriller filled with twists and turns that keep readers on the edge of their seats.")
            put(KEY_IMAGE_SOURCE, "drawable/book21")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book21)

        val book22 = ContentValues().apply {
            put(KEY_TITLE, "The Exorcist")
            put(KEY_AUTHOR, "William Peter Blatty")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Horror", db))
            put(KEY_RATING, "4.1/5")
            put(KEY_PRICE, "PHP 750")
            put(KEY_DESCRIPTION,"\"The Exorcist\" by William Peter Blatty is a classic horror novel that tells the story of a young girl, Regan, who becomes possessed by a demonic force. As her mother seeks help from priests, a battle between good and evil unfolds. Blatty's gripping narrative and exploration of faith make this a terrifying yet thought-provoking read.")
            put(KEY_IMAGE_SOURCE, "drawable/book22")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book22)

        val book23 = ContentValues().apply {
            put(KEY_TITLE, "Snow Crash")
            put(KEY_AUTHOR, "Neal Stephenson")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Sci-Fi", db))
            put(KEY_RATING, "4.1/5")
            put(KEY_PRICE, "PHP 760")
            put(KEY_DESCRIPTION, "Neal Stephenson's \"Snow Crash\" is a cyberpunk masterpiece set in a fragmented America. Hiro Protagonist, a skilled hacker, and Y.T., a street-savvy courier, uncover a conspiracy involving a virtual drug called Snow Crash. The novel delves into a world where information is a drug, corporations hold immense power, and linguistic theory intersects with virtual reality. Stephenson blends fast-paced action, satirical social commentary, and a techno-linguistic mystery, creating a narrative that remains prescient in its exploration of the digital age's societal implications.the")
            put(KEY_IMAGE_SOURCE, "drawable/book23")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book23)

        val book24 = ContentValues().apply {
            put(KEY_TITLE, "The Glass Menagerie")
            put(KEY_AUTHOR, "Tennessee Williams")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Drama", db))
            put(KEY_RATING, "4.4/5")
            put(KEY_PRICE, "PHP 830")
            put(KEY_DESCRIPTION, "\"The Glass Menagerie\" by Tennessee Williams is a poignant drama that delves into the lives of the Wingfield family. Focused on the dreams and struggles of each family member, the play explores themes of memory, illusion, and the search for identity. Williams' evocative language brings the characters to life on the stage.")
            put(KEY_IMAGE_SOURCE, "drawable/book24")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book24)

        val book25 = ContentValues().apply {
            put(KEY_TITLE, "Factfulness: Ten Reasons We're Wrong About the World – and Why Things Are Better Than You Think")
            put(KEY_AUTHOR, "Hans Rosling, Ola Rosling, Anna Rosling Rönnlund")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Non-Fiction", db))
            put(KEY_RATING, "4.8/5")
            put(KEY_PRICE, "PHP 900")
            put(KEY_DESCRIPTION, "\"Factfulness\" by Hans Rosling, Ola Rosling, and Anna Rosling Rönnlund challenges common misconceptions about the state of the world...")
            put(KEY_IMAGE_SOURCE, "drawable/book25")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book25)

        val book26 = ContentValues().apply {
            put(KEY_TITLE, "Moby-Dick")
            put(KEY_AUTHOR, "Herman Melville")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Classics", db))
            put(KEY_RATING, "4.5/5")
            put(KEY_PRICE, "PHP 700")
            put(KEY_DESCRIPTION, "\"Moby-Dick\" by Herman Melville is an epic tale that follows Captain Ahab's obsessive quest for revenge against the elusive white whale, Moby Dick...")
            put(KEY_IMAGE_SOURCE, "drawable/book26")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book26)

        val book27 = ContentValues().apply {
            put(KEY_TITLE, "The Fellowship of the Ring")
            put(KEY_AUTHOR, "J.R.R. Tolkien")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Fantasy", db))
            put(KEY_RATING, "4.9/5")
            put(KEY_PRICE, "PHP 800")
            put(KEY_DESCRIPTION, "\"The Fellowship of the Ring\" by J.R.R. Tolkien is the first volume in \"The Lord of the Rings\" trilogy...")
            put(KEY_IMAGE_SOURCE, "drawable/book27")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book27)

        val book28 = ContentValues().apply {
            put(KEY_TITLE, "Divergent")
            put(KEY_AUTHOR, "Veronica Roth")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Young Adult", db))
            put(KEY_RATING, "4.3/5")
            put(KEY_PRICE, "PHP 700")
            put(KEY_DESCRIPTION, "\"Divergent\" by Veronica Roth is a thrilling young adult novel set in a dystopian world divided into factions based on personality traits. Protagonist Tris Prior discovers she is Divergent, defying categorization, and becomes entangled in a dangerous conspiracy. Roth's action-packed narrative and exploration of identity make this book a compelling read.")
            put(KEY_IMAGE_SOURCE, "drawable/book28")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book28)


        val book29 = ContentValues().apply {
            put(KEY_TITLE, "The Da Vinci Code")
            put(KEY_AUTHOR, "Dan Brown")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Crime", db))
            put(KEY_RATING, "4.0/5")
            put(KEY_PRICE, "PHP 750")
            put(KEY_DESCRIPTION,"\"The Da Vinci Code\" by Dan Brown is a bestselling crime mystery that follows symbologist Robert Langdon and cryptologist Sophie Neveu as they unravel a series of ancient mysteries and uncover a secret that could shake the foundations of Christianity. Brown's fast-paced narrative and cryptic puzzles make this book a thrilling ride.")
            put(KEY_IMAGE_SOURCE, "drawable/book29")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book29)

        val book30 = ContentValues().apply {
            put(KEY_TITLE, "Bird Box")
            put(KEY_AUTHOR, "Josh Malerman")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Horror", db))
            put(KEY_RATING, "4.0/5")
            put(KEY_PRICE, "PHP 720")
            put(KEY_DESCRIPTION, "\"Bird Box\" by Josh Malerman is a psychological horror novel set in a world where supernatural entities cause people who see them to go insane and commit suicide. The story follows Malorie, who must navigate the perilous journey blindfolded to survive. Malerman's suspenseful storytelling keeps readers on the edge of their seats.")
            put(KEY_IMAGE_SOURCE, "drawable/book30")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book30)
        val book31 = ContentValues().apply {
            put(KEY_TITLE, "The Left Hand of Darkness")
            put(KEY_AUTHOR, "Ursula K. Le Guin")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Sci-Fi", db))
            put(KEY_RATING, "4.3/5")
            put(KEY_PRICE, "PHP 800")
            put(KEY_DESCRIPTION, "In Ursula K. Le Guin's \"The Left Hand of Darkness,\" Genly Ai, an envoy from the Ekumen, navigates the intricacies of the planet Gethen. Le Guin introduces readers to a world where the inhabitants can change genders, blurring the lines of traditional gender identity. Against the backdrop of a harsh Gethenian winter, political tensions, and the challenge of diplomatic relations, Genly faces questions of identity, loyalty, and cultural understanding. Le Guin's exploration of androgyny and societal structures makes this novel a groundbreaking work in speculative fiction.")
            put(KEY_IMAGE_SOURCE, "drawable/book31")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book31)

        val book32 = ContentValues().apply {
            put(KEY_TITLE, "A Thousand Splendid Suns")
            put(KEY_AUTHOR, "Khaled Hosseini")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Drama", db))
            put(KEY_RATING, "4.7/5")
            put(KEY_PRICE, "PHP 880")
            put(KEY_DESCRIPTION, "\"A Thousand Splendid Suns\" by Khaled Hosseini is a moving drama that portrays the lives of two Afghan women, Mariam and Laila, against the backdrop of political upheaval. The novel explores themes of friendship, sacrifice, and the resilience of the human spirit. Hosseini's rich storytelling captures the complexities of human relationships.")
            put(KEY_IMAGE_SOURCE, "drawable/book32")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book32)

        val book33 = ContentValues().apply {
            put(KEY_TITLE, "Thinking, Fast and Slow")
            put(KEY_AUTHOR, "Daniel Kahneman")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Non-Fiction", db))
            put(KEY_RATING, "4.6/5")
            put(KEY_PRICE, "PHP 850")
            put(KEY_DESCRIPTION, "\"Thinking, Fast and Slow\" by Daniel Kahneman explores the two systems that drive the way we think: the fast, intuitive, and emotional system, and the slow, deliberate, and logical system...")
            put(KEY_IMAGE_SOURCE, "drawable/book33")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book33)

        val book34 = ContentValues().apply {
            put(KEY_TITLE, "Wuthering Heights")
            put(KEY_AUTHOR, "Emily Brontë")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Classics", db))
            put(KEY_RATING, "4.6/5")
            put(KEY_PRICE, "PHP 680")
            put(KEY_DESCRIPTION, "\"Wuthering Heights\" by Emily Brontë is a dark and passionate novel that unfolds the tragic love story of Heathcliff and Catherine Earnshaw...")
            put(KEY_IMAGE_SOURCE, "drawable/book34")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book34)

        val book35 = ContentValues().apply {
            put(KEY_TITLE, "Harry Potter and the Sorcerer's Stone")
            put(KEY_AUTHOR, "J.K. Rowling")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Fantasy", db))
            put(KEY_RATING, "4.7/5")
            put(KEY_PRICE, "PHP 750")
            put(KEY_DESCRIPTION, "\"Harry Potter and the Sorcerer's Stone\" by J.K. Rowling introduces readers to the magical world of Hogwarts and the young wizard Harry Potter. As Harry navigates his first year at Hogwarts, he uncovers the mysteries of his past and confronts the dark wizard Voldemort. Rowling's enchanting narrative and imaginative world-building make the \"Harry Potter\" series a beloved fantasy phenomenon.")
            put(KEY_IMAGE_SOURCE, "drawable/book35")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book35)

        val book36 = ContentValues().apply {
            put(KEY_TITLE, "The Perks of Being a Wallflower")
            put(KEY_AUTHOR, "Stephen Chbosky")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Young Adult", db))
            put(KEY_RATING, "4.2/5")
            put(KEY_PRICE, "PHP 670")
            put(KEY_DESCRIPTION, "\"The Perks of Being a Wallflower\" by Stephen Chbosky is a coming-of-age young adult novel that follows the experiences of Charlie, an introverted high school freshman. Through letters to an anonymous friend, Charlie navigates the challenges of adolescence, friendship, and self-discovery. Chbosky's sensitive portrayal of teenage life resonates with readers.")
            put(KEY_IMAGE_SOURCE, "drawable/book36")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book36)


        val book37 = ContentValues().apply {
            put(KEY_TITLE, "The Girl on the Train")
            put(KEY_AUTHOR, "Paula Hawkins")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Crime", db))
            put(KEY_RATING, "4.1/5")
            put(KEY_PRICE, "PHP 770")
            put(KEY_DESCRIPTION,"\"The Girl on the Train\" by Paula Hawkins is a compelling crime thriller that follows the intertwined lives of three women. As the story unfolds through their perspectives, secrets and lies come to light, leading to a suspenseful and thrilling narrative.")
            put(KEY_IMAGE_SOURCE, "drawable/book37")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book37)

        val book38 = ContentValues().apply {
            put(KEY_TITLE, "House of Leaves")
            put(KEY_AUTHOR, "Mark Z. Danielewski")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Horror", db))
            put(KEY_RATING, "4.2/5")
            put(KEY_PRICE, "PHP 780")
            put(KEY_DESCRIPTION, "\"House of Leaves\" by Mark Z. Danielewski is a unique and unsettling horror novel that presents a labyrinthine narrative through multiple layers of storytelling. As a young family discovers unusual dimensions within their new home, the novel explores the psychological horror of the unknown. Danielewski's experimental style adds to the eerie atmosphere.")
            put(KEY_IMAGE_SOURCE, "drawable/book38")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book38)

        val book39 = ContentValues().apply {
            put(KEY_TITLE, "Hyperion")
            put(KEY_AUTHOR, "Dan Simmons")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Sci-Fi", db))
            put(KEY_RATING, "4.4/5")
            put(KEY_PRICE, "PHP 820")
            put(KEY_DESCRIPTION, "\"Hyperion\" unfolds as seven penilgrims share their life stories during a journey to the distant world of Hyperion, where a mysterious entity known as the Shrike awaits. Dan Simmons crafts a narrative mosaic, with each pilgrim offering a unique perspective on the universe's complexities. The stories intertwine to reveal overarching themes of destiny, artificial intelligence, and the nature of time. Simmons combines elements of science fiction, mythology, and philosophy to create a richly layered narrative that invites readers to ponder profound questions about the human experience.")
            put(KEY_IMAGE_SOURCE, "drawable/book39")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book39)

        val book40 = ContentValues().apply {
            put(KEY_TITLE, "The Great Gatsby")
            put(KEY_AUTHOR, "F. Scott Fitzgerald")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Drama", db))
            put(KEY_RATING, "4.8/5")
            put(KEY_PRICE, "PHP 890")
            put(KEY_DESCRIPTION, "\"The Great Gatsby\" by F. Scott Fitzgerald is a classic drama set in the Roaring Twenties. The novel follows the mysterious Jay Gatsby and his pursuit of wealth and love, as narrated by Nick Carraway. Fitzgerald's exploration of the American Dream, decadence, and disillusionment makes this novel a timeless examination of society and human nature.")
            put(KEY_IMAGE_SOURCE, "drawable/book40")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book40)

        val book41 = ContentValues().apply {
            put(KEY_TITLE, "Educated")
            put(KEY_AUTHOR, "Tara Westover")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Non-Fiction", db))
            put(KEY_RATING, "4.5/5")
            put(KEY_PRICE, "PHP 950")
            put(KEY_DESCRIPTION, "\"Educated\" by Tara Westover is a memoir that recounts the author's journey from growing up in a strict and abusive household in rural Idaho to eventually earning a PhD from Cambridge University. The book explores themes of education, resilience, and the pursuit of knowledge against all odds.")
            put(KEY_IMAGE_SOURCE, "drawable/book41")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book41)

        val book42 = ContentValues().apply {
            put(KEY_TITLE, "1984")
            put(KEY_AUTHOR, "George Orwell")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Classics", db))
            put(KEY_RATING, "4.7/5")
            put(KEY_PRICE, "PHP 850")
            put(KEY_DESCRIPTION, "George Orwell's \"1984\" is a dystopian novel that explores the dangers of totalitarianism and the consequences of a society under constant surveillance. It remains a powerful commentary on political oppression, censorship, and the struggle for individual freedom.")
            put(KEY_IMAGE_SOURCE, "drawable/book42")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book42)

        val book43 = ContentValues().apply {
            put(KEY_TITLE, "The Lies of Locke Lamora")
            put(KEY_AUTHOR, "Scott Lynch")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Fantasy", db))
            put(KEY_RATING, "4.3/5")
            put(KEY_PRICE, "PHP 830")
            put(KEY_DESCRIPTION, "\"The Lies of Locke Lamora\" by Scott Lynch is a gripping fantasy novel that unfolds in the sprawling, Venetian-inspired city of Camorr. The story revolves around Locke Lamora, a skilled and cunning thief who leads a group of con artists known as the Gentlemen Bastards. In the elaborate world of crime and corruption, the Gentlemen Bastards carry out complex heists and confidence schemes, targeting the city's wealthy elite. However, their intricate plans take an unexpected turn when a mysterious figure known as the Gray King disrupts the delicate balance of power in Camorr, putting Locke and his companions in mortal danger. As they navigate through a world of political intrigue, underworld dealings, and magical mysteries, Locke must rely on his wit and cunning to outsmart enemies and survive the perilous challenges that unfold. Lynch weaves a tale of suspense, humor, and intrigue, creating a richly detailed setting with a cast of memorable characters. The narrative unfolds with flashbacks that provide insight into Locke's upbringing and the formation of the Gentlemen Bastards, adding layers to the characters and the overarching plot. With its clever dialogue, intricate plotting, and a touch of the fantastical, \"The Lies of Locke Lamora\" offers readers a thrilling and immersive experience in a city full of secrets, crime, and unexpected alliances.")
            put(KEY_IMAGE_SOURCE, "drawable/book43")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book43)

        val book44 = ContentValues().apply {
            put(KEY_TITLE, "Simon vs. the Homo Sapiens Agenda")
            put(KEY_AUTHOR, "Becky Albertalli")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Young Adult", db))
            put(KEY_RATING, "4.4/5")
            put(KEY_PRICE, "PHP 720")
            put(KEY_DESCRIPTION, "\"Simon vs. the Homo Sapiens Agenda\" by Becky Albertalli is a heartwarming young adult novel that follows Simon Spier, a high school junior, as he navigates love, friendship, and coming out. The book explores themes of identity, acceptance, and the importance of being true to oneself.")
            put(KEY_IMAGE_SOURCE, "drawable/book44")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book44)

        val book45 = ContentValues().apply {
            put(KEY_TITLE, "In the Woods")
            put(KEY_AUTHOR, "Tana French")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Crime", db))
            put(KEY_RATING, "4.0/5")
            put(KEY_PRICE, "PHP 740")
            put(KEY_DESCRIPTION, "Tana French's \"In the Woods\" is a gripping crime novel featuring detective Rob Ryan. When a young girl is found murdered in a Dublin suburb, Ryan and his partner investigate, uncovering connections to Ryan's own troubled past. The novel explores psychological complexity and the impact of unresolved mysteries.")
            put(KEY_IMAGE_SOURCE, "drawable/book45")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book45)

        val book46 = ContentValues().apply {
            put(KEY_TITLE, "Pet Sematary")
            put(KEY_AUTHOR, "Stephen King")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Horror", db))
            put(KEY_RATING, "4.2/5")
            put(KEY_PRICE, "PHP 790")
            put(KEY_DESCRIPTION, "Stephen King's \"Pet Sematary\" is a chilling horror novel that explores the dark side of grief and loss. When a family moves to a rural town, they discover a mysterious burial ground with the power to bring the dead back to life. King delves into the consequences of tampering with the natural order.")
            put(KEY_IMAGE_SOURCE, "drawable/book46")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book46)

        val book47 = ContentValues().apply {
            put(KEY_TITLE, "Ender's Game")
            put(KEY_AUTHOR, "Orson Scott Card")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Sci-Fi", db))
            put(KEY_RATING, "4.3/5")
            put(KEY_PRICE, "PHP 810")
            put(KEY_DESCRIPTION, "Orson Scott Card's \"Ender's Game\" is a compelling exploration of interstellar conflict and the moral dilemmas of warfare. The story follows Andrew \"Ender\" Wiggin, a gifted child recruited into a military training program to defend Earth from an alien species. Ender's strategic brilliance propels him to a leadership role, but the ethical implications of his training raise poignant questions about the cost of victory and the manipulation of young minds for military purposes. Card weaves a tale of tactical genius, moral ambiguity, and the consequences of decisions made in the crucible of war.")
            put(KEY_IMAGE_SOURCE, "drawable/book47")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book47)

        val book48 = ContentValues().apply {
            put(KEY_TITLE, "The Road")
            put(KEY_AUTHOR, "Cormac McCarthy")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Drama", db))
            put(KEY_RATING, "4.2/5")
            put(KEY_PRICE, "PHP 800")
            put(KEY_DESCRIPTION, "Cormac McCarthy's \"The Road\" is a post-apocalyptic novel that follows a father and son's journey across a desolate landscape. As they navigate the harsh realities of survival, the novel explores themes of hope, morality, and the enduring bond between parent and child.")
            put(KEY_IMAGE_SOURCE, "drawable/book48")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book48)

        val book49 = ContentValues().apply {
            put(KEY_TITLE, "The Immortal Life of Henrietta Lacks")
            put(KEY_AUTHOR, "Rebecca Skloot")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Non-Fiction", db))
            put(KEY_RATING, "4.2/5")
            put(KEY_PRICE, "PHP 880")
            put(KEY_DESCRIPTION, "\"The Immortal Life of Henrietta Lacks\" by Rebecca Skloot tells the true story of Henrietta Lacks, whose cells were taken without her knowledge in 1951 and became one of the most important tools in medicine, leading to numerous scientific breakthroughs. The book explores ethical questions surrounding medical research and the impact on Henrietta's family.")
            put(KEY_IMAGE_SOURCE, "drawable/book49")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book49)

        val book50 = ContentValues().apply {
            put(KEY_TITLE, "The Catcher in the Rye")
            put(KEY_AUTHOR, "J.D. Salinger")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Classics", db))
            put(KEY_RATING, "4.3/5")
            put(KEY_PRICE, "PHP 780")
            put(KEY_DESCRIPTION, "J.D. Salinger's \"The Catcher in the Rye\" is a coming-of-age novel that follows the experiences of Holden Caulfield, a disenchanted teenager navigating the challenges of adolescence and adulthood. The novel is known for its exploration of alienation, identity, and societal expectations.")
            put(KEY_IMAGE_SOURCE, "drawable/book50")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book50)

        val book51 = ContentValues().apply {
            put(KEY_TITLE, "The Way of Kings")
            put(KEY_AUTHOR, "Brandon Sanderson")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Fantasy", db))
            put(KEY_RATING, "4.6/5")
            put(KEY_PRICE, "PHP 850")
            put(KEY_DESCRIPTION, "Brandon Sanderson's \"The Way of Kings\" is the first book in the Stormlight Archive series. The epic fantasy novel introduces readers to the world of Roshar, where magical storms shape the land and political intrigue unfolds. Sanderson's masterful storytelling and intricate plot make this a must-read for fantasy enthusiasts.")
            put(KEY_IMAGE_SOURCE, "drawable/book51")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book51)

        val book52 = ContentValues().apply {
            put(KEY_TITLE, "The Maze Runner")
            put(KEY_AUTHOR, "James Dashner")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Young Adult", db))
            put(KEY_RATING, "4.1/5")
            put(KEY_PRICE, "PHP 690")
            put(KEY_DESCRIPTION, "James Dashner's \"The Maze Runner\" is a thrilling young adult science fiction novel. The story revolves around a group of teenagers who wake up in a mysterious maze with no memory of how they got there. As they work together to solve the maze's puzzles, they uncover dark secrets about their predicament.")
            put(KEY_IMAGE_SOURCE, "drawable/book52")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book52)

        val book53 = ContentValues().apply {
            put(KEY_TITLE, "Mystic River")
            put(KEY_AUTHOR, "Dennis Lehane")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Crime", db))
            put(KEY_RATING, "4.2/5")
            put(KEY_PRICE, "PHP 770")
            put(KEY_DESCRIPTION, "\"Mystic River\" by Dennis Lehane is a gripping crime novel that explores the lives of three childhood friends whose paths diverge after a traumatic event. When tragedy strikes again years later, their lives become intertwined once more, leading to a complex and suspenseful narrative. Lehane's masterful storytelling and deep exploration of human nature make \"Mystic River\" a compelling crime thriller.")
            put(KEY_IMAGE_SOURCE, "drawable/book53")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book53)

        val book54 = ContentValues().apply {
            put(KEY_TITLE, "The Haunting of Hill House")
            put(KEY_AUTHOR, "Shirley Jackson")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Horror", db))
            put(KEY_RATING, "4.0/5")
            put(KEY_PRICE, "PHP 750")
            put(KEY_DESCRIPTION, "Shirley Jackson's \"The Haunting of Hill House\" is a classic haunted house tale. A small group of people are invited to stay at the ominous Hill House to investigate paranormal activities. As the supernatural events unfold, the line between reality and the supernatural blurs.")
            put(KEY_IMAGE_SOURCE, "drawable/book54")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book54)

        val book55 = ContentValues().apply {
            put(KEY_TITLE, "The Time Machine")
            put(KEY_AUTHOR, "H.G. Wells")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Sci-Fi", db))
            put(KEY_RATING, "4.1/5")
            put(KEY_PRICE, "PHP 730")
            put(KEY_DESCRIPTION, "H.G. Wells' \"The Time Machine\" stands as a foundational work in science fiction literature, introducing the concept of time travel. The unnamed Time Traveller invents a machine that propels him into the distant future, where he encounters two distinct human species—the Eloi and the Morlocks. Wells uses this temporal journey to explore profound themes of social evolution, the consequences of unchecked progress, and the relativity of time itself. The novella's enduring impact lies in its capacity to provoke contemplation on the trajectory of human civilization and the implications of tampering with the fabric of time.")
            put(KEY_IMAGE_SOURCE, "drawable/book55")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book55)

        val book56 = ContentValues().apply {
            put(KEY_TITLE, "The Joy Luck Club")
            put(KEY_AUTHOR, "Amy Tan")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Drama", db))
            put(KEY_RATING, "4.0/5")
            put(KEY_PRICE, "PHP 760")
            put(KEY_DESCRIPTION, "Amy Tan's \"The Joy Luck Club\" is a poignant exploration of Chinese-American identity and intergenerational relationships. The novel weaves together the stories of Chinese immigrant mothers and their American-born daughters, delving into cultural clashes and the complexities of family dynamics.")
            put(KEY_IMAGE_SOURCE, "drawable/book56")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book56)

    }


    @SuppressLint("Range")
    fun getCategoryID(categoryName: String, db: SQLiteDatabase?): Int {
        val selection = "$KEY_CATEGORY_NAME = ?"
        val selectionArgs = arrayOf(categoryName)
        val cursor = db?.query(TABLE_CATEGORIES, arrayOf(KEY_CATEGORY_ID), selection, selectionArgs, null, null, null)

        return cursor?.use {
            if (it.moveToFirst()) {
                it.getInt(it.getColumnIndex(KEY_CATEGORY_ID))
            } else {
                // If category not found, return a default value or handle appropriately
                -1
            }
        } ?: -1
    }
    @SuppressLint("Range")
    fun getBooksByCategory(categoryId: Int): List<Details> {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_DETAILS WHERE $KEY_CATEGORY_ID_FK = ?"
        val selectionArgs = arrayOf(categoryId.toString())

        val cursor = db.rawQuery(query, selectionArgs)
        val books = mutableListOf<Details>()

        cursor.use {
            while (it.moveToNext()) {
                val book = Details(
                    it.getInt(it.getColumnIndex(KEY_DETAILS_ID)),
                    it.getString(it.getColumnIndex(KEY_TITLE)),
                    it.getString(it.getColumnIndex(KEY_AUTHOR)),
                    it.getInt(it.getColumnIndex(KEY_CATEGORY_ID_FK)),
                    it.getString(it.getColumnIndex(KEY_IMAGE_SOURCE)),
                    it.getString(it.getColumnIndex(KEY_RATING)),
                    it.getString(it.getColumnIndex(KEY_PRICE)),
                    it.getString(it.getColumnIndex(KEY_DESCRIPTION)),
                    it.getInt(it.getColumnIndex(KEY_ADD_TO_LIBRARY)) !=0,
                    it.getInt(it.getColumnIndex(KEY_ADD_TO_FAVE)) !=0
                )
                books.add(book)
            }
        }

        db.close()
        return books
    }
    @SuppressLint("Range")
    fun getCategoryNameById(categoryId: Int): String {
        val db = this.readableDatabase
        val selection = "$KEY_CATEGORY_ID = ?"
        val selectionArgs = arrayOf(categoryId.toString())
        val cursor = db.query(TABLE_CATEGORIES, arrayOf(KEY_CATEGORY_NAME), selection, selectionArgs, null, null, null)

        var categoryName = ""

        cursor.use {
            if (it.moveToFirst()) {
                categoryName = it.getString(it.getColumnIndex(KEY_CATEGORY_NAME))
            }
        }

        db.close()
        return categoryName
    }
    @SuppressLint("Range")
    fun getLibraryBooks(userEmail: String): List<Details> {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_DETAILS WHERE $KEY_ADD_TO_LIBRARY = 1 AND $KEY_DETAILS_ID IN (SELECT $KEY_BOOK_ID_FK FROM $TABLE_LIBRARY WHERE $KEY_USER_EMAIL_FK = ?)"

        val cursor = db.rawQuery(query, arrayOf(userEmail))
        val libraryBooks = mutableListOf<Details>()

        cursor.use {
            while (it.moveToNext()) {
                val book = Details(
                    it.getInt(it.getColumnIndex(KEY_DETAILS_ID)),
                    it.getString(it.getColumnIndex(KEY_TITLE)),
                    it.getString(it.getColumnIndex(KEY_AUTHOR)),
                    it.getInt(it.getColumnIndex(KEY_CATEGORY_ID_FK)),
                    it.getString(it.getColumnIndex(KEY_IMAGE_SOURCE)),
                    it.getString(it.getColumnIndex(KEY_RATING)),
                    it.getString(it.getColumnIndex(KEY_PRICE)),
                    it.getString(it.getColumnIndex(KEY_DESCRIPTION)),
                    it.getInt(it.getColumnIndex(KEY_ADD_TO_LIBRARY)) != 0,
                    it.getInt(it.getColumnIndex(KEY_ADD_TO_FAVE)) != 0
                )
                libraryBooks.add(book)
            }
        }

        db.close()
        return libraryBooks
    }

    @SuppressLint("Range")
    fun addToLibrary(userEmail: String, bookId: Int): Boolean {
        val db = this.writableDatabase

        val isInLibrary = isBookInLibrary(userEmail, bookId)

        if (!isInLibrary) {
            val values = ContentValues()
            values.put(KEY_USER_EMAIL_FK, userEmail)
            values.put(KEY_BOOK_ID_FK, bookId)

            // Insert a new record in the library table
            db.insert(TABLE_LIBRARY, null, values)

            // Update the addToLibrary field in the details table
            val updateValues = ContentValues()
            updateValues.put(KEY_ADD_TO_LIBRARY, 1)
            db.update(TABLE_DETAILS, updateValues, "$KEY_DETAILS_ID=?", arrayOf(bookId.toString()))
        }

        db.close()
        return !isInLibrary
    }

    @SuppressLint("Range")
    private fun isBookInLibrary(userEmail: String, bookId: Int): Boolean {
        val db = this.readableDatabase
        val selection = "$KEY_USER_EMAIL_FK = ? AND $KEY_BOOK_ID_FK = ?"
        val selectionArgs = arrayOf(userEmail, bookId.toString())
        val cursor = db.query(TABLE_LIBRARY, null, selection, selectionArgs, null, null, null)

        val isInLibrary = cursor.count > 0
        cursor.close()

        return isInLibrary
    }

    @SuppressLint("Range")
    fun removeFromLibrary(userEmail: String, bookId: Int): Boolean {
        val db = this.writableDatabase

        // Check if the book is in the library for the specific user
        val isInLibrary = isBookInLibrary(userEmail, bookId)

        if (isInLibrary) {
            // Delete the record from the library table
            db.delete(TABLE_LIBRARY, "$KEY_USER_EMAIL_FK = ? AND $KEY_BOOK_ID_FK = ?", arrayOf(userEmail, bookId.toString()))
        }

        db.close()
        return isInLibrary // Return true if the book was in the library, false otherwise
    }
}