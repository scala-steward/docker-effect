package docker.effect
package interop

trait RioMonad[F[-_, +_]] extends RioFunctor[F] {
  def pure[R, A](a: A): F[R, A]
  def >>=[R, A, B](fa: F[R, A])(f: A => F[R, B]): F[R, B]
  def >>>[RA, A, RB >: A, B](fa: F[RA, A])(next: F[RB, B]): F[RA, B]

  val unit: F[Any, Unit] = pure(())
}

object RioMonad {
  @inline final def apply[F[-_, +_]](implicit ev: RioMonad[F]): RioMonad[F] = ev
}
