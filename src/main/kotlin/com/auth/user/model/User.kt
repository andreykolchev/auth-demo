package com.auth.user.model

import com.auth.authorization.Authority
import jakarta.persistence.*
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Audited
@Table(name = "user_details")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Column(name = "username")
    private val username: String = "",

    @Column(name = "password")
    @NotAudited
    private val password: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "authority")
    private val authority: Authority = Authority.USER,

    @Column(name = "login_count")
    private var loginCount: Long = 0
) : UserDetails {

    override fun getAuthorities(): List<SimpleGrantedAuthority> {
        return listOf(SimpleGrantedAuthority(authority.name))
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    fun incrementLoginCount() {
        loginCount++
    }
}