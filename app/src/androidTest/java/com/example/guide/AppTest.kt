package com.example.guide

import android.content.Context
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.Description
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.internal.matchers.TypeSafeMatcher
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class AppTest {

    @Rule
    @JvmField
    val activityRule = ActivityScenarioRule(OpenActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testLoginSuccess() {

        val context = ApplicationProvider.getApplicationContext<Context>()
        val mockUserDao = AppDatabase.getDataBase(context).userDao()
        runBlocking {
            val user = User(login = "example_login", pass = "example_password")
            mockUserDao.insert(user)

        }
        val mockRepository = UsersRepository(mockUserDao)

        val mockViewModel = UserViewModel(ApplicationProvider.getApplicationContext())

        mockViewModel.repository = mockRepository

        // Передаем мок UserViewModel в активность
        activityRule.scenario.onActivity { activity ->
            activity.userViewModel = mockViewModel
        }

        // Выполняем действия на экране
        onView(withId(R.id.userLogin)).perform(typeText("example_login"), closeSoftKeyboard())
        onView(withId(R.id.userPass)).perform(typeText("example_password"), closeSoftKeyboard())
        onView(withId(R.id.enterBtn)).perform(click())

        // Проверяем, что активность MainActivity была запущена
        intended(hasComponent(MainActivity::class.java.name))

    }

    @Test
    fun testLoginFailure() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val mockUserDao = AppDatabase.getDataBase(context).userDao()
        runBlocking {
            val user = User(login = "new_login", pass = "new_password")
            mockUserDao.insert(user)

        }
        val mockRepository = UsersRepository(mockUserDao)

        val mockViewModel = UserViewModel(ApplicationProvider.getApplicationContext())

        mockViewModel.repository = mockRepository

        // Передаем мок UserViewModel в активность
        activityRule.scenario.onActivity { activity ->
            activity.userViewModel = mockViewModel
        }


        // Выполняем действия на экране
        onView(withId(R.id.userLogin)).perform(typeText("invalid_login"), closeSoftKeyboard())
        onView(withId(R.id.userPass)).perform(typeText("invalid_password"), closeSoftKeyboard())
        onView(withId(R.id.enterBtn)).perform(click())

        onView(withText("Пользователь не авторизован")).inRoot(ToastMatcher()).check(matches(isDisplayed()))

    }
}

class ToastMatcher : TypeSafeMatcher<Root>() {
    override fun describeTo(description: Description) {
        description.appendText("is toast")
    }

    override fun matchesSafely(root: Root): Boolean {
        val type = root.windowLayoutParams.get().type
        if (type == WindowManager.LayoutParams.TYPE_TOAST) {
            val windowToken = root.decorView.windowToken
            val appToken = root.decorView.applicationWindowToken
            return windowToken === appToken
        }
        return false
    }
}

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var userDao: UserDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        userDao = db.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() {
        runBlocking {
            val user = User(id = 2, login = "new_login", pass = "new_password")
            userDao.insert(user)

            val byName = userDao.getAll()
            assertThat(byName, hasItem(user))

        }

    }
}

class TestActivity : AppCompatActivity()

/*@RunWith(AndroidJUnit4::class)
class NotesFragmentTest {

    @get:Rule
    //var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var scenario: FragmentScenario<NotesFragment>

    private lateinit var notesAdapter: NotesAdapter

    @Before
    fun setup() {
        // Initialize the ViewModel with a mock UserViewModel
        val userViewModel = mock(UserViewModel::class.java)
        val noteViewModel = mock(NoteViewModel::class.java)

        // Initialize the adapter with a mock list of Notes
        notesAdapter = NotesAdapter()
        notesAdapter.setData(
            listOf(
                Note(1, "Title 1", "Description 1", 1),
                Note(2, "Title 2", "Description 2", 1)
            )
        )

        // Initialize the fragment with the mock ViewModels and adapter
        scenario = launchFragmentInContainer(
            //themeResId = R.style.Theme_NotesApp,
            fragmentArgs = null,
            factory = object : FragmentFactory() {
                override fun instantiate(classLoader: ClassLoader, className: String) =
                    NotesFragment().apply {
                        this.userViewModel = userViewModel
                        this.noteViewModel = noteViewModel
                        notesAdapter = notesAdapter
                    }
            }
        )
        scenario.moveToState(Lifecycle.State.RESUMED)
    }

    *//*@Test
    fun testNotesDisplayed() {
        fun testNotesDisplayed() {
            onView(withId(R.id.recyclerView)).check(
                matches(
                    RecyclerViewItemAtPositionAssertion(
                        0,
                        allOf(
                            withText("Title 1"),
                            withParent(withId(R.id.recyclerView))
                        )
                    )
                )
            )

        onView(withId(R.id.recyclerView)).check(
            RecyclerViewItemAtPositionAssertion(
                1,
                allOf(
                    withText("Title 2"),
                    withParent(withId(R.id.recyclerView))
                )
            )
        )
    }*//*

    @Test
    fun testDeleteAllNotesDialogDisplayed() {
        onView(withId(R.id.delete_menu)).perform(click())

        onView(withText("Удалить все заметки?")).check(matches(isDisplayed()))
    }

    *//*@Test
    fun testDeleteAllNotes() {
        onView(withId(R.id.delete_menu)).perform(click())

        onView(withText("Да")).perform(click())

        onView(withId(R.id.recyclerView)).check(RecyclerViewItemCountAssertion(0))
    }
}

class RecyclerViewItemCountAssertion(private val expectedCount: Int) :
    BaseMatcher<View>(), ViewAssertion {

    override fun matches(item: Any?): Boolean {
        if (item !is RecyclerView) {
            return false
        }

        return item.adapter?.itemCount == expectedCount
    }

    override fun describeTo(description: Description) {
        description.appendText("RecyclerView item count should be $expectedCount")
    }
}*//*

    class RecyclerViewItemAtPositionAssertion(
        private val position: Int,
        private val matcher: Matcher<View>
    ) : BaseMatcher<View>() {

        override fun matches(item: Any?): Boolean {
            if (item !is RecyclerView) {
                return false
            }

            val view = item.findViewHolderForAdapterPosition(position)?.itemView

            return view != null && matcher.matches(view)
        }

        override fun describeTo(description: Description) {
            description.appendText("RecyclerView item at position $position should match $matcher")
        }
    }
}


@RunWith(AndroidJUnit4::class)
class NotesFragmentTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(OpenActivity::class.java)

    @Test
    fun testActivityState() {
        val scenario = activityRule.scenario
        scenario.onActivity { activity ->
            assertThat(activity.lifecycle.currentState).isEqualTo(Lifecycle.State.RESUMED)
        }
    }

    @Test
    fun testLoginButton() {
        // Type in some login and password
        onView(withId(R.id.userLogin)).perform(typeText("testuser"))
        onView(withId(R.id.userPass)).perform(typeText("testpass"))

        // Click the "Войти" button
        onView(withId(R.id.enterBtn)).perform(click())

        // Wait for the MainActivity to be launched
        intended(hasComponent(MainActivity::class.java.name))

        // Check that the user ID is passed to the MainActivity
        val extras = Intent.getIntent(InstrumentationRegistry.getInstrumentation().targetContext.toString()).extras
        assertThat(extras?.getLong("userId"), not(equalTo(-1)))
    }
}

 */
