#lang racket

;; A functional complement to Visitor: each constructor returns a closure that
;; bundles every operation for that node type. Existing clients know only the
;; message protocol, so a new node type needs no changes to those clients.

(provide literal binary evaluate pretty-print to-rpn)

(define (send node operation)
  (node operation))

(define (literal value)
  (lambda (operation)
    (case operation
      [(evaluate) value]
      [(pretty) (format "~a" value)]
      [(rpn) (format "~a" value)]
      [else (error 'literal "unknown operation: ~a" operation)])))

(define (binary left operator right)
  (lambda (operation)
    (case operation
      [(evaluate)
       (define left-value (send left 'evaluate))
       (define right-value (send right 'evaluate))
       (case operator
         [(+) (+ left-value right-value)]
         [(-) (- left-value right-value)]
         [(*) (* left-value right-value)]
         [(/) (/ left-value right-value)]
         [else (error 'binary "unknown operator: ~a" operator)])]
      [(pretty)
       (format "(~a ~a ~a)"
               (send left 'pretty) operator (send right 'pretty))]
      [(rpn)
       (format "~a ~a ~a"
               (send left 'rpn) (send right 'rpn) operator)]
      [else (error 'binary "unknown operation: ~a" operation)])))

(define (evaluate expression)
  (send expression 'evaluate))

(define (pretty-print expression)
  (send expression 'pretty))

(define (to-rpn expression)
  (send expression 'rpn))

(module+ test
  (require rackunit)
  (define example
    (binary (binary (literal 1) '+ (literal 2))
            '*
            (binary (literal 4) '- (literal 3))))
  (check-equal? (evaluate example) 3)
  (check-equal? (pretty-print example) "((1 + 2) * (4 - 3))")
  (check-equal? (to-rpn example) "1 2 + 4 3 - *"))

(module+ main
  (define example
    (binary (binary (literal 1) '+ (literal 2))
            '*
            (binary (literal 4) '- (literal 3))))
  (displayln (pretty-print example))
  (displayln (to-rpn example))
  (displayln (evaluate example)))
