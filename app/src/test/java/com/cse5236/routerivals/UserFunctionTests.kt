import com.cse5236.routerivals.TestUserRepository
import com.cse5236.routerivals.model.User
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserRepositoryUnitTest {

    private lateinit var repo: TestUserRepository

    @Before
    fun setup() {
        repo = TestUserRepository()
    }

    @Test
    fun addUser_savesCorrectly() = runTest {
        val user = User(id = "u1", name = "Alice", email = "a@test.com")
        repo.saveUser(user)

        val stored = repo.getUser("u1")
        assertNotNull(stored)
        assertEquals("Alice", stored?.name)
        assertEquals("a@test.com", stored?.email)
    }

    @Test
    fun addFriend_updatesFriendListOfBothUsers() = runTest {
        val u1 = User(id = "u1", name = "Alice", friends = emptyList())
        val u2 = User(id = "u2", name = "Bob", friends = emptyList())

        repo.saveUser(u1)
        repo.saveUser(u2)

        // Simulate "accept friend request" since that updates both sides
        repo.acceptFriendRequest(acceptingUid = "u1", requestingUid = "u2")

        val updated1 = repo.getUser("u1")
        val updated2 = repo.getUser("u2")

        assertEquals(listOf("u2"), updated1?.friends)
        assertEquals(listOf("u1"), updated2?.friends)
    }

    @Test
    fun removeFriend_removesFromBothUsers() = runTest {
        val u1 = User(id = "u1", friends = listOf("u2"))
        val u2 = User(id = "u2", friends = listOf("u1"))

        repo.saveUser(u1)
        repo.saveUser(u2)

        repo.removeFriend("u1", "u2")

        val after1 = repo.getUser("u1")
        val after2 = repo.getUser("u2")

        assertTrue(after1?.friends?.isEmpty() == true)
        assertTrue(after2?.friends?.isEmpty() == true)
    }
}
