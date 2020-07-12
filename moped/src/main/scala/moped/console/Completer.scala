package moped.console

trait Completer[A] {
  def complete(context: TabCompletionContext): List[TabCompletionItem]
}
