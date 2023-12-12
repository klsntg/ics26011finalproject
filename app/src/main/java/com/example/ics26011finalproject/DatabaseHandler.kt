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
            put(KEY_DESCRIPTION, "In the realm of high fantasy, Patrick Rothfuss's \"The Name of the Wind\" stands as a testament to the genre's capacity for intricate storytelling and profound character development. The narrative unfolds through the lens of the enigmatic Kvothe, a gifted musician and magician with a complex past. What sets this novel apart is its framing device—the protagonist narrates his own life story, adding layers of mystery and introspection. Rothfuss's prose is nothing short of poetic, creating an immersive atmosphere where the harmonies of music and the complexities of magic intertwine seamlessly. From the hallowed halls of the University to the elusive Chandrian, the novel weaves a tapestry of intrigue, inviting readers into a world where every note and every word holds profound significance.")
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
            put(KEY_DESCRIPTION,"\"The Hunger Games\" by Suzanne Collins is a gripping dystopian novel set in the bleak future of Panem, a totalitarian society comprised of the Capitol and twelve impoverished districts. The story unfolds as Katniss Everdeen, a resourceful young woman from District 12, volunteers to take her sister Prim's place in the annual Hunger Games, a brutal televised event where tributes must fight to the death until only one survives. Alongside her fellow tribute, Peeta Mellark, Katniss enters a deadly arena where alliances, strategy, and survival instincts are paramount. As the Games progress, Katniss becomes a symbol of defiance against the oppressive Capitol, unintentionally sparking a rebellion. The novel explores themes of survival, sacrifice, and the consequences of unchecked power, all against the backdrop of a society that thrives on control and spectacle. Suzanne Collins crafts a thrilling narrative that balances action-packed sequences with thoughtful reflection on the human cost of entertainment and political manipulation. \"The Hunger Games\" captivates readers with its intricate world-building, strong character development, and a relentless pace, making it a compelling and thought-provoking exploration of the human spirit in the face of adversity.")
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
            put(KEY_DESCRIPTION,"\"The Girl with the Dragon Tattoo\" by Stieg Larsson is a riveting psychological thriller that unfolds against the backdrop of a chilling mystery. The story follows investigative journalist Mikael Blomkvist, who is hired by wealthy industrialist Henrik Vanger to unravel the decades-old disappearance of his niece, Harriet. As Blomkvist delves into the Vanger family's dark history on the isolated Hedeby Island, he joins forces with the enigmatic hacker Lisbeth Salander, a brilliant and unconventional investigator with a troubled past. Together, they uncover a web of corruption, family secrets, and violence that stretches back generations. The novel skillfully intertwines Blomkvist's pursuit of the truth with Salander's own struggles against a society that has mistreated her. Larsson crafts a compelling narrative that explores themes of power, abuse, and justice while keeping readers on the edge of their seats with unexpected twists and turns. \"The Girl with the Dragon Tattoo\" is a masterfully plotted and emotionally charged novel that combines elements of crime fiction, social commentary, and character-driven drama to create a gripping and unforgettable tale.")
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
            put(KEY_DESCRIPTION,"Stephen King's \"The Shining\" is a masterfully crafted horror novel that delves into the psychological unraveling of Jack Torrance, who, along with his family, becomes the winter caretaker of the isolated Overlook Hotel. The story unfolds against the backdrop of the eerie and snowbound hotel, with Jack's son, Danny, possessing a psychic ability known as \"the shining.\" As the hotel's malevolent forces gradually consume Jack's sanity, his descent into madness becomes a gripping exploration of isolation, supernatural terror, and the haunting legacy of the Overlook's dark history. King skillfully blends psychological horror with supernatural elements, creating an atmosphere of mounting dread and tension. The haunted hotel itself becomes a character, with its labyrinthine corridors and sinister presence amplifying the psychological horror. \"The Shining\" is a chilling and iconic work in the horror genre, showcasing King's ability to tap into primal fears and leaving an indelible mark on the landscape of literary and cinematic horror.")
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
            put(KEY_DESCRIPTION,"\"The Kite Runner\" by Khaled Hosseini is a poignant and emotionally resonant novel that explores themes of friendship, betrayal, redemption, and the impact of personal choices against the backdrop of Afghanistan's tumultuous history. The story is narrated by Amir, a privileged boy from Kabul, and it unfolds against the backdrop of significant events, including the Soviet invasion and the rise of the Taliban. At the heart of the narrative is Amir's complex relationship with his servant's son, Hassan. The bond between the two boys is tested by a traumatic incident that haunts Amir into adulthood. The novel weaves a tapestry of guilt, forgiveness, and the quest for redemption as Amir returns to a changed Afghanistan to confront his past. Hosseini's rich storytelling and evocative prose capture the human cost of political upheaval and personal choices, making \"The Kite Runner\" a powerful exploration of love and redemption amidst the complexities of cultural and familial ties. The novel's universal themes and compelling characters have made it a literary sensation and a moving testament to the enduring impact of personal and historical legacies.")
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
            put(KEY_DESCRIPTION,"\n The Power of Habit\" by Charles Duhigg is a captivating exploration of the intricate mechanisms governing our behaviors and the profound impact habits have on our lives. Duhigg masterfully weaves together scientific research, compelling anecdotes, and real-world examples to unveil the hidden patterns that shape routines. Through a keen analysis of the habit loop—cue, routine, reward—readers gain valuable insights into the neurological and psychological factors that drive habit formation and how habits can be transformed. One of the book's strengths lies in its practical approach to habit change. Duhigg introduces the concept of \"keystone habits,\" which act as catalysts for broader positive transformations. By dissecting the habits of individuals, organizations, and societies, he illustrates the power of intentional habit modification. Whether exploring corporate success stories or personal struggles, \"The Power of Habit\" imparts a profound understanding of the mechanics of habits and equips readers with the knowledge to reshape their lives through intentional behavior change.")
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
            put(KEY_DESCRIPTION,"\n" +
                    "\"Pride and Prejudice\" by Jane Austen is a captivating exploration of love, societal norms, and individual growth in early 19th-century England. The novel follows the spirited Elizabeth Bennet as she navigates the challenges of societal expectations and the complexities of romantic entanglements. Austen's keen observations and witty narrative unveil the dynamics of class and manners, offering a timeless critique of a society obsessed with marriage and social status. The story unfolds with the arrival of the enigmatic Mr. Darcy, whose pride clashes with Elizabeth's initial prejudice, leading to a journey of self-discovery and transformation for both characters. Filled with memorable characters, sharp social commentary, and Austen's signature irony, \"Pride and Prejudice\" remains a beloved classic, celebrated for its enduring relevance and timeless exploration of the human heart.")
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
            put(KEY_DESCRIPTION,"\"The Fault in Our Stars\" is a heartrending contemporary novel written by John Green. The story revolves around two teenagers, Hazel Grace Lancaster and Augustus Waters, who meet at a cancer support group. Hazel, who has thyroid cancer that has spread to her lungs, and Augustus, who is in remission after losing a leg to the disease, form a deep and poignant connection. As they navigate the challenges of living with illness, their relationship evolves into a powerful exploration of love, loss, and the meaning of life. The novel not only delves into the emotional complexities of facing mortality but also examines the impact of literature and philosophy on the characters' perceptions of their own existence. John Green skillfully weaves humor, wit, and heartbreak into a narrative that resonates with readers, creating a beautiful and emotionally charged story that transcends the boundaries of the young adult genre. \"The Fault in Our Stars\" has become a beloved and critically acclaimed novel, touching the hearts of readers around the world, and was also adapted into a successful film.")
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
            put(KEY_DESCRIPTION,"\"The Silence of the Lambs\" by Thomas Harris is a psychological horror novel that introduces readers to the brilliant but insane psychiatrist, Dr. Hannibal Lecter. The narrative centers around FBI trainee Clarice Starling, who is assigned to interview Dr. Lecter in the hope of gaining insights into the mind of another serial killer, Buffalo Bill. Buffalo Bill is on the loose, murdering young women and leaving behind gruesome clues. As Clarice engages in a psychological chess game with the incarcerated Lecter, she becomes entangled in a complex and disturbing investigation. The novel skillfully weaves together elements of crime, horror, and psychological suspense, exploring the dark recesses of the human psyche. Thomas Harris creates a chilling atmosphere, building tension through meticulous details and character development. \"The Silence of the Lambs\" is a gripping and unsettling exploration of criminal psychology, featuring unforgettable characters and a narrative that keeps readers on the edge of their seats until the shocking conclusion. The novel has left an indelible mark on the thriller genre and was adapted into a highly acclaimed film that further solidified its place in popular culture.")
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
            put(KEY_DESCRIPTION,"Stephen King's \"It\" is a sprawling and nightmarish tale that weaves together horror, nostalgia, and the enduring power of friendship. Set in the small town of Derry, the novel oscillates between two timelines, chronicling the lives of a group of children in the 1950s who confront a malevolent entity that preys on their deepest fears, and their adult selves returning to Derry in the 1980s to face the resurgence of the same monstrous force. At the heart of the story is Pennywise, a shape-shifting entity that often takes the form of a clown and terrorizes the children. King skillfully delves into the psyches of his characters, exploring the impact of trauma, the complexities of memory, and the enduring bonds forged in the crucible of shared terror. The novel's rich characterizations, evocative setting, and relentless tension contribute to its status as a horror classic, exploring not only the supernatural but also the very real fears and scars that linger into adulthood. \"It\" stands as a testament to King's storytelling prowess, creating an immersive and chilling experience that continues to haunt readers long after they turn the final page.")
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
            put(KEY_DESCRIPTION, "\"To Kill a Mockingbird\" by Harper Lee is a timeless and impactful novel that explores themes of racial injustice, moral growth, and compassion in the American South during the 1930s. Narrated by Scout Finch, a young girl growing up in the fictional town of Maycomb, Alabama, the story centers around her father, Atticus Finch, a principled lawyer defending Tom Robinson, a Black man falsely accused of raping a white woman. The novel unfolds as Scout and her brother, Jem, witness the complexities of racial prejudice, social inequality, and moral ambiguity in their community. Through the trial of Tom Robinson, Harper Lee addresses the deeply ingrained racism of the time and challenges societal norms. The novel is also a coming-of-age story, as Scout and Jem confront the harsh realities of the world around them and learn valuable lessons about empathy and standing up for what is right. \"To Kill a Mockingbird\" is celebrated for its poignant narrative, memorable characters, and its enduring relevance as a powerful exploration of justice and morality. The novel won the Pulitzer Prize for Fiction and has become a classic of American literature, studied in schools for its social commentary and literary merit.")
            put(KEY_IMAGE_SOURCE, "drawable/book16")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book16)

        val book17 = ContentValues().apply {
            put(KEY_TITLE, "Astrophysics")
            put(KEY_AUTHOR, "Neil deGrasse Tyson")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Non-Fiction", db))
            put(KEY_RATING, "4.5/5")
            put(KEY_PRICE, "PHP 750")
            put(KEY_DESCRIPTION, "Neil deGrasse Tyson's \"Astrophysics for People in a Hurry\" offers a condensed yet enlightening journey through the vast expanse of the cosmos, catering to those with a curiosity for the universe but limited time. Tyson, a renowned astrophysicist and science communicator, adeptly distills complex astronomical concepts into bite-sized explanations accessible to a broad audience. From the Big Bang to black holes, dark matter, and the nature of time, Tyson covers a range of cosmic phenomena, emphasizing the wonders of the universe with wit and clarity.In this cosmic odyssey, readers encounter the beauty of celestial bodies, the mysteries of dark energy, and the concept of spacetime. Tyson's engaging narrative not only imparts fundamental astrophysical knowledge but also sparks a sense of awe and wonder, encouraging readers to contemplate their place in the cosmos. Through \"Astrophysics for People in a Hurry,\" Tyson succeeds in making the complexities of astrophysics digestible, ensuring that even those with time constraints can embark on a captivating exploration of the cosmos.")
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
            put(KEY_DESCRIPTION, "Pride and Prejudice by Jane Austen is a timeless novel that delves into the intricacies of 19th-century English society. The narrative unfolds through the experiences of Elizabeth Bennet, a spirited and intelligent young woman, as she navigates the societal norms, expectations, and prejudices prevalent in her time. The central focus of the story is Elizabeth's evolving relationship with the enigmatic Mr. Darcy. Initially marred by misunderstandings and societal pressures, their love story explores themes of personal growth, societal critique, and the complexities of class dynamics. Austen's sharp wit and keen observations provide a satirical commentary on the manners and customs of the era, making \"Pride and Prejudice\" not merely a romance but a social critique that remains relevant across generations.Jane Eyre by Charlotte Brontë is a compelling coming-of-age novel that follows the life of the eponymous protagonist, Jane Eyre. Orphaned and mistreated in her early years, Jane rises above adversity to become a governess. The novel is a nuanced exploration of themes such as morality, societal expectations, and the quest for personal independence. The love story between Jane and the brooding Mr. Rochester unfolds against the backdrop of Thornfield Hall, shrouded in mystery and gothic elements. Brontë's narrative skillfully intertwines the personal and societal struggles of its characters, creating a poignant tale of resilience, self-discovery, and the pursuit of true love.")
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
            put(KEY_DESCRIPTION, "Game of Thrones\" is a fantasy epic by George R.R. Martin that has gained immense popularity both as a series of novels and a television adaptation. The story is set in the fictional continents of Westeros and Essos and follows numerous characters vying for control of the Iron Throne and power over the Seven Kingdoms. With intricate political plots, complex characters, and a richly detailed world, the series explores themes of power, loyalty, honor, and the consequences of ambition. The unpredictability of character deaths and the morally ambiguous nature of many figures make \"Game of Thrones\" a gripping tale that subverts traditional fantasy tropes.")
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
            put(KEY_DESCRIPTION, "\"To All the Boys I've Loved Before\" is a charming young adult romance novel written by Jenny Han. The story follows Lara Jean Covey, a high school junior who has a habit of writing secret love letters to all the boys she has ever had a crush on but never intends to send them. However, her world is turned upside down when the letters are accidentally mailed out, leading to a series of unexpected events.To avoid confronting her real feelings, Lara Jean enters into a fake relationship with one of the recipients of her letters, Peter Kavinsky, a popular and charming lacrosse player. As they navigate the challenges of their pretend relationship, both characters discover more about themselves and each other. The novel explores themes of love, identity, and the complexities of relationships, all within the backdrop of high school life.Jenny Han's storytelling is characterized by its authenticity and relatability, making \"To All the Boys I've Loved Before\" a delightful and heartwarming read for young adult audiences. The novel has gained widespread popularity and was adapted into a successful film by Netflix.")
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
            put(KEY_DESCRIPTION,"\"Gone Girl\" by Gillian Flynn is a gripping psychological thriller that explores the complexities of marriage and the dark side of human nature. The story revolves around Nick and Amy Dunne, a seemingly perfect couple whose lives take a sinister turn on their fifth wedding anniversary when Amy goes missing under mysterious circumstances. As the media frenzy and police investigation unfold, the narrative takes an unexpected twist, revealing the intricacies of Nick and Amy's relationship and the secrets they both harbor. The novel skillfully alternates between Nick's present-day perspective and Amy's diary entries, providing contrasting viewpoints that keep readers guessing about the truth. Flynn masterfully constructs a narrative that delves into themes of deception, media scrutiny, and the unpredictable dynamics of intimate relationships. The plot is filled with twists and turns, challenging readers' perceptions and leaving them questioning the motivations of the characters. \"Gone Girl\" is a dark and suspenseful exploration of the fragility of love and trust, making it a compelling and thought-provoking read that captivates until its chilling and unexpected conclusion. The novel was also adapted into a successful film directed by David Fincher.")
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
            put(KEY_DESCRIPTION,"\"The Exorcist\" by William Peter Blatty is a landmark horror novel that plunges readers into the depths of supernatural terror and religious despair. Set against the backdrop of Georgetown, the story unfolds with the possession of a twelve-year-old girl named Regan MacNeil. As her behavior becomes increasingly erratic and disturbing, her mother, Chris, seeks medical and psychiatric help, only to find that Regan's affliction goes beyond the realm of conventional understanding. Desperate, Chris turns to Father Damien Karras, a Jesuit priest who is struggling with his own crisis of faith. Blatty masterfully builds an atmosphere of dread, exploring themes of good versus evil, faith versus doubt, and the pervading fear of the unknown. The novel's intense and chilling sequences, coupled with its exploration of the spiritual and psychological dimensions of possession, have solidified its place as a seminal work in horror literature. \"The Exorcist\" continues to be a cultural touchstone, inspiring both fear and fascination, and its impact extends beyond the realm of literature, as it was famously adapted into a highly successful and iconic film directed by William Friedkin.")
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
            put(KEY_DESCRIPTION, "\"The Glass Menagerie\" by Tennessee Williams is a poignant and introspective play that delves into the lives of the Wingfield family, grappling with the harsh realities of dreams deferred and the delicate balance between illusion and reality. Set in Depression-era St. Louis, the play is narrated by Tom Wingfield, who reflects on his memories of living with his overbearing mother, Amanda, and his emotionally fragile sister, Laura. Amanda yearns for a better life for her children, pressuring Tom to find a suitor for Laura. However, Laura is painfully shy and finds solace in her collection of glass animal figurines. The play explores themes of escapism, the consequences of unfulfilled ambitions, and the weight of familial expectations. Williams crafts a poetic and emotionally charged narrative, blending realism with elements of memory and symbolism. \"The Glass Menagerie\" is celebrated for its rich characterizations, intricate dialogue, and its exploration of the universal struggle for identity and fulfillment. The play remains a classic of American theater, offering a timeless portrayal of the complexities of family dynamics and the delicate nature of human aspirations.")
            put(KEY_IMAGE_SOURCE, "drawable/book24")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book24)

        val book25 = ContentValues().apply {
            put(KEY_TITLE, "Factfulness")
            put(KEY_AUTHOR, "Hans Rosling, Ola Rosling, Anna Rosling Rönnlund")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Non-Fiction", db))
            put(KEY_RATING, "4.8/5")
            put(KEY_PRICE, "PHP 900")
            put(KEY_DESCRIPTION, "In \"Factfulness,\" Hans Rosling, along with co-authors Ola Rosling and Anna Rosling Rönnlund, challenges prevailing misconceptions about the state of the world and advocates for a more fact-based worldview. Drawing on extensive global data, Rosling dismantles commonly held myths about issues like poverty, population growth, and health, showcasing how the world has made significant progress in many areas. The authors introduce the concept of \"factfulness,\" emphasizing the importance of embracing a nuanced understanding of the world that acknowledges positive developments while addressing persistent challenges.Through engaging anecdotes and illustrative graphs, Rosling illustrates how preconceived notions often lead to a distorted perception of global realities. The book not only provides a refreshing perspective on global trends but also serves as a guide for critical thinking and media literacy. \"Factfulness\" invites readers to reassess their understanding of the world, promoting a more accurate and optimistic outlook that encourages informed decision-making and fosters a greater appreciation for the progress humanity has achieved.\n")
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
            put(KEY_DESCRIPTION, "\"Moby-Dick\" by Herman Melville is a monumental work that transcends the boundaries of traditional storytelling. At its core, the novel is an epic tale of Captain Ahab's relentless pursuit of the elusive white whale, Moby Dick, driven by an all-consuming desire for revenge. However, Melville's narrative expands beyond a simple revenge story, delving into profound philosophical and existential questions. The novel is a kaleidoscopic exploration of the human condition, touching on themes of obsession, the clash between nature and civilization, and the inherent complexities of morality. The diverse cast of characters and Melville's rich prose contribute to the enduring legacy of \"Moby-Dick\" as a profound literary masterpiece.")
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
            put(KEY_DESCRIPTION, "\"The Fellowship of the Ring\" is the first volume of J.R.R. Tolkien's epic fantasy trilogy, \"The Lord of the Rings.\" The story begins with the unassuming hobbit, Frodo Baggins, inheriting a powerful ring that could bring about the destruction of Middle-earth if it falls into the wrong hands. Frodo sets out on a perilous journey with a diverse group of characters, known as the Fellowship, to destroy the ring and thwart the dark lord Sauron. Tolkien's work is celebrated for its world-building, intricate languages, and profound themes of friendship, sacrifice, and the battle between good and evil.")
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
            put(KEY_DESCRIPTION, "\"Divergent\" is a young adult science fiction novel written by Veronica Roth. Set in a future dystopian society, the story takes place in a city divided into five factions, each representing a different virtue: Abnegation (selflessness), Amity (peacefulness), Candor (honesty), Dauntless (bravery), and Erudite (intelligence). At the age of sixteen, every citizen must choose a faction to which they will belong for the rest of their lives.The protagonist, Beatrice \"Tris\" Prior, discovers that she is \"Divergent,\" meaning she does not fit neatly into any single faction. This realization puts her in danger as it challenges the rigid social structure and threatens the stability of the society. Tris decides to leave her family's faction, Abnegation, and join Dauntless, where she undergoes rigorous training and faces various challenges.As Tris navigates her new life, she uncovers dark secrets about the society and faces the looming threat of unrest and rebellion. The novel explores themes of identity, conformity, and the consequences of a society obsessed with categorization.Divergent\" is the first book in a trilogy, followed by \"Insurgent\" and \"Allegiant.\" The series has been popular among young adult readers and was adapted into a film series.")
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
            put(KEY_DESCRIPTION,"\"The Da Vinci Code\" by Dan Brown is a fast-paced and intricately plotted thriller that combines art, history, and religion into a compelling narrative. The story follows Harvard symbologist Robert Langdon as he becomes entangled in a murder investigation at the Louvre Museum in Paris. The victim has left a series of cryptic codes and symbols that Langdon, along with cryptologist Sophie Neveu, must decipher to unravel a deeper mystery. As they follow the trail of clues across Europe, they uncover a secret society, religious conspiracies, and hidden messages within famous works of art, all while trying to solve the puzzle that could shake the foundations of Christianity.Dan Brown weaves together elements of art history, symbology, and religious lore to create a suspenseful and thought-provoking tale. \"The Da Vinci Code\" explores themes of faith, knowledge, and the blurred line between fact and fiction. The novel has been praised for its intricate plot, unexpected twists, and the integration of historical and artistic references. While it has been criticized for its controversial interpretation of religious history, the book's popularity has made it a cultural phenomenon, sparking widespread discussion and debate. The success of \"The Da Vinci Code\" also led to a film adaptation directed by Ron Howard, further cementing its place as a modern thriller classic.")
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
            put(KEY_DESCRIPTION, "\"Bird Box\" by Josh Malerman is a gripping and atmospheric horror novel that introduces readers to a world where an unknown force drives people to madness and violence upon sight. The story follows Malorie, a young mother, and her two children as they navigate a perilous journey blindfolded, all in an effort to reach a rumored haven and escape the malevolent entities that have decimated the world's population. The novel alternates between the present-day journey and the initial outbreak of the mysterious phenomenon, building suspense and a sense of impending danger. Malerman's narrative plays on the primal fear of the unseen, creating a tense and claustrophobic atmosphere as characters grapple with the challenges of survival in a sightless world. \"Bird Box\" is a psychological thriller that explores themes of fear, motherhood, and the extremes to which people will go to protect their loved ones, leaving readers on the edge of their seats until the chilling conclusion. The novel was adapted into a successful film by Netflix, further cementing its popularity.")
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
            put(KEY_DESCRIPTION, "\"A Thousand Splendid Suns\" by Khaled Hosseini is a powerful and heart-wrenching novel that weaves together the lives of two Afghan women, Mariam and Laila, against the backdrop of the war-torn landscape of Afghanistan. The narrative unfolds over several decades, exploring the complexities of their interconnected destinies. Mariam, born out of wedlock and burdened by a sense of rejection, becomes entwined with Laila, a young woman from a more privileged background, when their lives intersect due to the tumultuous events of the Afghan conflict. Through their enduring friendship, the novel explores themes of resilience, sacrifice, and the strength of the human spirit in the face of adversity. The story delves into the harsh realities of gender-based oppression, war, and the indomitable bonds that form between women despite the hardships they endure. Khaled Hosseini's evocative storytelling captures the intricacies of the Afghan culture and society, making \"A Thousand Splendid Suns\" a profound and emotionally resonant exploration of love and sacrifice amidst the backdrop of a troubled nation.")
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
            put(KEY_DESCRIPTION, "\"Thinking, Fast and Slow\" is a groundbreaking work by Nobel laureate Daniel Kahneman that explores the two systems of thought that govern human decision-making. Kahneman divides thinking into two systems: System 1, which is fast, intuitive, and emotional, and System 2, which is slow, deliberate, and logical. The book delves into the cognitive biases and errors that influence decision-making, revealing how individuals often deviate from rationality. Kahneman draws on decades of research in psychology and behavioral economics to provide insights into how people make choices in various aspects of life, from economic decisions to personal relationships. \"Thinking, Fast and Slow\" has had a profound impact on the understanding of human behavior and decision science.")
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
            put(KEY_DESCRIPTION, "\"Wuthering Heights\" by Emily Brontë is a dark and passionate exploration of love, revenge, and the destructive power of unchecked emotions. Set against the haunting backdrop of the Yorkshire moors, the novel unfolds the tumultuous love affair between Heathcliff and Catherine Earnshaw. Brontë's narrative is layered with Gothic elements, capturing the eerie and atmospheric essence of Wuthering Heights, the manor that becomes a symbol of the novel's brooding intensity. The story, told through a series of narratives, explores the cyclical nature of revenge and the enduring impact of unchecked passions. \"Wuthering Heights\" stands as a haunting portrayal of the complexities of human relationships, leaving an indelible mark on literature.")
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
            put(KEY_DESCRIPTION, "J.K. Rowling's \"Harry Potter and the Sorcerer's Stone\" introduces readers to the enchanting world of Hogwarts through the eyes of the eponymous character. The novel seamlessly blends elements of mystery, friendship, and the classic hero's journey. Rowling's wit and creativity shine in her creation of a magical society replete with its own rules, creatures, and societal intricacies. Beyond the allure of magic, the novel's success lies in its exploration of universal themes—friendship, loyalty, and the timeless battle between good and evil. As Harry navigates the challenges of his newfound identity, readers are drawn into a narrative that transcends age and continues to captivate generations.")
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
            put(KEY_DESCRIPTION, "\"The Perks of Being a Wallflower\" by Stephen Chbosky is a poignant coming-of-age novel that delves into the complexities of adolescence, mental health, and the search for identity. Narrated through the letters of the introspective and observant protagonist, Charlie, the novel follows his journey as he navigates the challenges of high school, forming deep connections with his new friends, Sam and Patrick. As Charlie grapples with past trauma and emotional struggles, the story unfolds with a mix of heartwarming moments, humor, and a genuine exploration of the human experience. Chbosky artfully captures the universal themes of love, friendship, and self-discovery, making \"The Perks of Being a Wallflower\" a timeless and emotionally resonant work that has resonated with readers for its authenticity and relatability. The novel has become a classic in the young adult genre, celebrated for its raw portrayal of the highs and lows of adolescence and the importance of finding one's place in the world.")
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
            put(KEY_DESCRIPTION,"\"The Girl on the Train\" by Paula Hawkins is a psychological thriller that unfolds through the perspectives of three women, each grappling with personal struggles and intertwined lives. The central character is Rachel Watson, an alcoholic woman who takes the same train every day and becomes obsessed with a seemingly perfect couple she observes from the train window. When the woman from the couple goes missing, Rachel becomes entangled in the investigation.As the narrative unfolds, it reveals the complexities of the characters' lives, their secrets, and the blurred lines between truth and perception. Hawkins skillfully builds tension, using unreliable narrators to keep readers guessing and creating an atmosphere of suspense. The novel explores themes of addiction, betrayal, and the impact of trauma on memory.The Girl on the Train\" received widespread acclaim for its gripping plot and psychological depth. The novel's success led to a film adaptation, further solidifying its popularity. The book's exploration of the dark and often unpredictable aspects of human nature has made it a standout in the psychological thriller genre.")
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
            put(KEY_DESCRIPTION, "\"House of Leaves\" by Mark Z. Danielewski is a labyrinthine and unconventional narrative that defies traditional storytelling. The novel presents a multilayered structure, featuring a story within a story within a story. The core narrative follows Johnny Truant, who discovers a manuscript written by a deceased blind man named Zampanò. This manuscript details the academic analysis of a documentary that doesn't seem to exist — a film about a mysterious house that defies the laws of physics. The house is bigger on the inside than the outside, and its exploration becomes an increasingly unsettling and existential journey. Danielewski employs unconventional formatting, footnotes, and a myriad of typographical techniques to create a physically disorienting reading experience that mirrors the psychological disintegration of the characters. The novel's intricate design and the interplay between narratives explore themes of obsession, existential dread, and the nature of reality, making \"House of Leaves\" a groundbreaking and immersive work that challenges conventional literary norms. Readers find themselves not just engaged in a story but also unraveling a complex tapestry of narratives that blur the lines between fiction and reality.")
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
            put(KEY_DESCRIPTION, "\"The Great Gatsby\" by F. Scott Fitzgerald is a classic American novel that paints a vivid portrait of the Roaring Twenties, exploring themes of wealth, love, and the elusive American Dream. The story is narrated by Nick Carraway, a young man who moves to Long Island and becomes entangled in the lives of his mysterious and wealthy neighbor, Jay Gatsby, and his cousin Daisy Buchanan, who lives across the bay. Gatsby is known for his extravagant parties and his unrequited love for Daisy, who is married to the wealthy but arrogant Tom Buchanan. As the narrative unfolds, the novel delves into the decadence and moral decay of the Jazz Age, revealing the emptiness that often lies beneath the glittering surface of wealth. Fitzgerald's prose is rich and lyrical, capturing the allure and disillusionment of the American Dream. \"The Great Gatsby\" is celebrated for its exploration of the complexities of human nature, social class, and the elusive nature of happiness, making it a timeless and enduring work in American literature.")
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
            put(KEY_DESCRIPTION, "Tara Westover's memoir, \"Educated,\" recounts her remarkable journey from a remote, survivalist family in rural Idaho to earning a PhD from the University of Cambridge. Raised by strict and abusive parents who distrusted the government and formal education, Tara and her siblings were denied access to schools, medical care, and a conventional upbringing. Despite these challenges, Tara's thirst for knowledge led her to self-educate and ultimately break free from her family's constraints. The narrative explores the tension between loyalty to one's roots and the pursuit of personal growth, as Tara grapples with the sacrifices required to forge her own path. As Tara gains access to formal education, she confronts not only the academic challenges but also the emotional toll of confronting her family's beliefs. \"Educated\" is a poignant exploration of the power of education to liberate and transform, but also the emotional complexities that accompany such a journey. Tara Westover's gripping narrative serves as a testament to the resilience of the human spirit and the profound impact that education can have on shaping one's identity and breaking the chains of a challenging past.")
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
            put(KEY_DESCRIPTION, "\"1984\" by George Orwell is a chilling dystopian novel that paints a stark and cautionary vision of a totalitarian society under the oppressive rule of the Party and its omnipresent leader, Big Brother. The story follows Winston Smith, a disillusioned member of the Outer Party who rebels against the regime's surveillance, propaganda, and thought control. Orwell's exploration of themes such as government surveillance, censorship, and the manipulation of truth remains eerily relevant in contemporary discussions about the erosion of individual freedoms. \"1984\" serves as a powerful critique of totalitarianism and a stark warning about the consequences of unchecked governmental power, leaving readers with a profound sense of the fragility of freedom and the importance of resisting oppressive systems.")
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
            put(KEY_DESCRIPTION, "\"Simon vs. the Homo Sapiens Agenda\" by Becky Albertalli is a heartwarming and humorous young adult novel that explores themes of identity, friendship, and love. The story revolves around Simon Spier, a sixteen-year-old high school junior, who is not quite ready to come out as gay. When an email falls into the wrong hands, threatening to expose his secret, Simon finds himself in a delicate situation. The novel takes readers on a journey of self-discovery as Simon navigates the challenges of high school life, friendship dynamics, and the complexities of romantic relationships. Becky Albertalli skillfully captures the voice of a teenage protagonist dealing with the universal struggles of acceptance and authenticity. The novel has been praised for its relatable characters, engaging narrative, and its positive representation of LGBTQ+ themes. \"Simon vs. the Homo Sapiens Agenda\" has resonated with readers for its warmth, humor, and the important message that everyone deserves love and acceptance for who they are.")
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
            put(KEY_DESCRIPTION, "\"In the Woods\" is a psychological mystery novel written by Tana French. The book is the first in the Dublin Murder Squad series. The story is set in Ireland and follows detective Rob Ryan as he investigates the murder of a young girl in a small town. What makes the case particularly challenging for Ryan is that he has a personal connection to the location—it's the same woods where, as a child, he was found alone and with no memory of what happened to his two childhood friends who disappeared.As Ryan delves into the current murder case, he grapples with his own past and the mysteries surrounding his childhood. The novel is known for its atmospheric writing, complex characters, and intricate plotting. Tana French weaves psychological elements into the mystery, exploring themes of memory, trauma, and the impact of the past on the present.In the Woods\" received critical acclaim for its compelling narrative and was awarded the Edgar Award for Best First Novel by an American Author. It marks the beginning of Tana French's successful career as a writer of psychological crime fiction.")
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
            put(KEY_DESCRIPTION, "\"Pet Sematary\" by Stephen King is a chilling and psychologically intense horror novel that delves into the darkness that lurks beyond the veil of death. The story follows the Creed family—Louis, Rachel, and their two children, Ellie and Gage—as they move to a rural town in Maine. Behind their new home lies a mysterious burial ground with the unsettling ability to bring the dead back to life. When tragedy strikes the family, Louis, driven by grief and desperation, makes a fateful decision to use the forbidden burial ground. The consequences of his actions unleash a malevolent force that challenges the boundaries between life and death. King masterfully explores themes of grief, loss, and the profound fear of mortality. The novel builds an atmosphere of dread and unease, leading to a horrifying climax that leaves readers questioning the limits of human desperation. \"Pet Sematary\" is a haunting exploration of the supernatural and the psychological toll of tampering with the natural order, cementing its place as one of Stephen King's most enduring and unsettling works.")
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
            put(KEY_DESCRIPTION, "\"The Road\" by Cormac McCarthy is a haunting and post-apocalyptic novel that follows the journey of a father and his young son across a desolate and devastated landscape. In a world ravaged by an unspecified disaster, the pair travels southward in the hope of finding safety and a semblance of humanity. The novel is an exploration of the father-son relationship amidst the bleakest of circumstances, as they face the constant threat of starvation, violence, and the harsh elements. McCarthy's spare and stark prose contributes to the novel's grim atmosphere, emphasizing the struggle for survival and the fragility of hope. \"The Road\" delves into profound existential questions about the nature of humanity, morality, and the meaning of life in the face of utter devastation. It's a powerful and emotionally resonant work that has been acclaimed for its literary merit and its poignant portrayal of the human spirit in the midst of despair.")
            put(KEY_IMAGE_SOURCE, "drawable/book48")
            put(KEY_ADD_TO_LIBRARY, 0)
            put(KEY_ADD_TO_FAVE, 0)
        }
        db?.insert(TABLE_DETAILS, null, book48)

        val book49 = ContentValues().apply {
            put(KEY_TITLE, "Immortal Life of Henrietta")
            put(KEY_AUTHOR, "Rebecca Skloot")
            put(KEY_CATEGORY_ID_FK, getCategoryID("Non-Fiction", db))
            put(KEY_RATING, "4.2/5")
            put(KEY_PRICE, "PHP 880")
            put(KEY_DESCRIPTION, "The Immortal Life of Henrietta Lacks\" by Rebecca Skloot is a compelling narrative that unravels the story of Henrietta Lacks, an African American woman whose cells were unknowingly taken for medical research in the 1950s. Henrietta's cells, known as HeLa cells, became one of the most important tools in medicine, contributing to numerous scientific breakthroughs, including the development of the polio vaccine and advancements in cancer research. Despite their significant impact, Henrietta and her family were never informed about the use of her cells, raising profound ethical questions about medical consent and the exploitation of individuals in the name of scientific progress.The book weaves together two narratives: one delves into the scientific legacy of HeLa cells, while the other explores the personal history of Henrietta and the emotional toll her unwitting contribution took on her family. Skloot navigates the complex intersection of science, ethics, and the human story, shedding light on the often overlooked individuals behind scientific advancements. \"The Immortal Life of Henrietta Lacks\" is not just a tale of scientific discovery but also a thought-provoking exploration of medical ethics, race, and the enduring impact on the lives of those connected to the remarkable story of Henrietta Lacks.")
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
            put(KEY_DESCRIPTION, "\"The Catcher in the Rye\" by J.D. Salinger is a coming-of-age novel that captures the angst and alienation of adolescence. The story is narrated by Holden Caulfield, a disenchanted and rebellious sixteen-year-old, who has been expelled from an elite boarding school. Over the course of a few days, Holden recounts his experiences in New York City, providing a raw and unfiltered commentary on the phoniness of the adult world and the struggles of finding authenticity in a society he perceives as insincere. Holden's distinctive voice, filled with cynicism and vulnerability, has resonated with readers for generations, making the novel a classic in American literature. Through Holden's journey, Salinger explores themes of identity, loss of innocence, and the search for meaning in a world that often seems superficial and indifferent.")
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
            put(KEY_DESCRIPTION, "Brandon Sanderson's \"The Way of Kings\" ushers readers into the breathtaking world of Roshar, a realm defined by a unique magic system, intricate political machinations, and a sprawling cast of characters. The narrative follows characters like Kaladin, Shallan, and Dalinar, each grappling with their destinies and the extraordinary challenges they face. Sanderson's meticulous world-building unveils diverse ecosystems, mysterious orders of Knights Radiant, and the looming threat of Desolation. Themes of leadership, destiny, and the consequences of wielding immense power are intricately woven into a narrative that promises an epic journey spanning multiple volumes. Sanderson's ability to seamlessly blend character-driven storytelling with grand-scale world-building makes \"The Way of Kings\" a cornerstone in contemporary fantasy literature.")
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
            put(KEY_DESCRIPTION, "\"The Maze Runner\" is a science fiction novel written by James Dashner. The story centers around a teenage boy named Thomas, who wakes up in a mysterious glade surrounded by a massive maze with no memory of his past. The glade is populated by a group of boys, each with no memory of how they arrived, and they have established a community with its own rules and hierarchy. The maze is inhabited by deadly creatures, and the boys have been trying to solve its mysteries for years.As Thomas becomes involved in the efforts to escape the maze, he discovers that he possesses unique abilities that may hold the key to their freedom. The novel is known for its suspenseful plot, filled with mystery and danger, as the characters face various challenges in their attempt to unravel the secrets of the maze and find a way out. The Maze Runner is the first book in a series, followed by \"The Scorch Trials,\" \"The Death Cure,\" and a prequel titled \"The Kill Order.\" The series gained popularity for its fast-paced narrative, intriguing world-building, and the twists and turns that keep readers on the edge of their seats. The success of the series led to film adaptations as well.")
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
            put(KEY_DESCRIPTION, "\"Mystic River\" is a psychological thriller novel written by Dennis Lehane. The story is set in the working-class neighborhoods of Boston and revolves around three childhood friends—Jimmy Marcus, Dave Boyle, and Sean Devine—whose lives are forever changed by a traumatic event in their youth.The narrative begins with the friends being reunited by another tragic incident: the murder of Jimmy's daughter. Sean, now a detective, is assigned to the case, and as he investigates, the dark secrets and painful memories from their shared past resurface. Dave, who was abducted and traumatized as a child, becomes a central figure in the investigation, adding layers of complexity to the story.Dennis Lehane explores themes of friendship, loyalty, guilt, and the far-reaching consequences of trauma. The novel is known for its intricate plot, well-developed characters, and the exploration of the psychological impact of past events on the present. \"Mystic River\" received critical acclaim and was adapted into a film directed by Clint Eastwood, further establishing its reputation as a powerful and compelling work in the genre of psychological crime fiction.")
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
            put(KEY_DESCRIPTION, "\"The Haunting of Hill House\" by Shirley Jackson is a classic and atmospheric haunted house tale that transcends traditional horror tropes to delve into the psychological and supernatural. The story follows a small group of people brought together by Dr. John Montague, an investigator of paranormal phenomena, as they spend a summer at Hill House, a mansion with a dark and mysterious history. As the characters begin to experience strange and terrifying occurrences, the novel masterfully blurs the lines between the supernatural and the psychological, leaving readers in a perpetual state of unease. Shirley Jackson's evocative prose and skillful exploration of fear and the unknown contribute to the enduring impact of \"The Haunting of Hill House.\" The novel has been celebrated for its ability to unsettle readers through subtle and psychological horror rather than relying on overt scares, making it a cornerstone of the horror genre. The influence of \"The Haunting of Hill House\" can be seen in various adaptations, including film and television, attesting to its enduring legacy in the realm of literary horror.")
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
            put(KEY_DESCRIPTION, "\"The Joy Luck Club\" by Amy Tan is a poignant exploration of the intricate tapestry of mother-daughter relationships within the context of Chinese-American immigrant experiences. Through a series of interconnected narratives, the novel navigates the complexities of cultural identity, generational divides, and the enduring bonds of family. Tan's storytelling is both tender and evocative as she weaves together the voices of the immigrant mothers, each bearing the weight of their pasts, with the voices of their American-born daughters striving to reconcile their dual identities. The Joy Luck Club, with its gatherings and mahjong games, becomes a metaphorical bridge between these two worlds, allowing the characters to share their hopes, dreams, and the profound impact of their intertwined histories. The novel is a celebration of resilience, cultural heritage, and the transformative power of understanding and empathy across generations.")
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