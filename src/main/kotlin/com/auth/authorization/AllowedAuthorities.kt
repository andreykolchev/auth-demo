package com.auth.authorization

/**
 * Specifies a list of allowed authorities.
 *
 * @property values An array of Authority objects representing the allowed authorities.
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class AllowedAuthorities(vararg val values: Authority)
