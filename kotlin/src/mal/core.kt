package mal

import java.io.File

val ns = hashMapOf(
        Pair(MalSymbol("+"), MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger + y as MalInteger }) })),
        Pair(MalSymbol("-"), MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger - y as MalInteger }) })),
        Pair(MalSymbol("*"), MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger * y as MalInteger }) })),
        Pair(MalSymbol("/"), MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger / y as MalInteger }) })),

        Pair(MalSymbol("list"), MalFunction({ a: ISeq -> MalList(a) })),
        Pair(MalSymbol("list?"), MalFunction({ a: ISeq -> if (a.first() is MalList) TRUE else FALSE })),
        Pair(MalSymbol("empty?"), MalFunction({
            a: ISeq -> if (a.first() !is ISeq || !(a.first() as ISeq).seq().any()) TRUE else FALSE
        })),
        Pair(MalSymbol("count"), MalFunction({
            a: ISeq -> if (a.first() is ISeq) MalInteger((a.first() as ISeq).count().toLong()) else MalInteger(0)
        })),

        Pair(MalSymbol("="), MalFunction({ a: ISeq -> pairwiseEquals(a) })),
        Pair(MalSymbol("<"), MalFunction({ a: ISeq -> pairwiseCompare(a, { x, y -> x.value < y.value }) })),
        Pair(MalSymbol("<="), MalFunction({ a: ISeq -> pairwiseCompare(a, { x, y -> x.value <= y.value }) })),
        Pair(MalSymbol(">"), MalFunction({ a: ISeq -> pairwiseCompare(a, { x, y -> x.value > y.value }) })),
        Pair(MalSymbol(">="), MalFunction({ a: ISeq -> pairwiseCompare(a, { x, y -> x.value >= y.value }) })),

        Pair(MalSymbol("pr-str"), MalFunction({
            a: ISeq -> MalString(a.seq().map({ it -> pr_str(it, print_readably = true) }).joinToString(" "))
        })),
        Pair(MalSymbol("str"), MalFunction({
            a: ISeq -> MalString(a.seq().map({ it -> pr_str(it, print_readably = false) }).joinToString(""))
        })),
        Pair(MalSymbol("prn"), MalFunction({
            a: ISeq -> println(a.seq().map({ it -> pr_str(it, print_readably = true) }).joinToString(" ")); NIL
        })),
        Pair(MalSymbol("println"), MalFunction({
            a: ISeq -> println(a.seq().map({ it -> pr_str(it, print_readably = false) }).joinToString(" ")); NIL
        })),

        Pair(MalSymbol("read-string"), MalFunction({ a: ISeq ->
            val string = a.first() as? MalString ?: throw MalException("slurp requires a string parameter")
            read_str(string.value)
        })),
        Pair(MalSymbol("slurp"), MalFunction({ a: ISeq ->
            val name = a.first() as? MalString ?: throw MalException("slurp requires a filename parameter")
            val text = File(name.value).readText()
            MalString(text)
        })),

        Pair(MalSymbol("cons"), MalFunction({ a: ISeq ->
            val list = a.nth(1) as? ISeq ?: throw MalException("cons requires a list as its second parameter")
            val mutableList = list.seq().toLinkedList()
            mutableList.addFirst(a.nth(0))
            MalList(mutableList)
        })),
        Pair(MalSymbol("concat"), MalFunction({ a: ISeq ->
            MalList(a.seq().flatMap({ it -> (it as ISeq).seq() }).toLinkedList())
        })),

        Pair(MalSymbol("nth"), MalFunction({ a: ISeq ->
            val list = a.nth(0) as? ISeq ?: throw MalException("nth requires a list as its first parameter")
            val index = a.nth(1) as? MalInteger ?: throw MalException("nth requires an integer as its second parameter")
            if (index.value >= list.seq().count()) throw MalException("index out of bounds")
            list.nth(index.value.toInt())
        })),
        Pair(MalSymbol("first"), MalFunction({ a: ISeq ->
            if (a.nth(0) == NIL) NIL
            else {
                val list = a.nth(0) as? ISeq ?: throw MalException("first requires a list parameter")
                if (list.seq().any()) list.first() else NIL
            }
        })),
        Pair(MalSymbol("rest"), MalFunction({ a: ISeq ->
            val list = a.nth(0) as? ISeq ?: throw MalException("rest requires a list parameter")
            MalList(list.rest())
        })),

        Pair(MalSymbol("throw"), MalFunction({ a: ISeq ->
            val throwable = a.nth(0)
            throw MalCoreException(pr_str(throwable), throwable)
        })),

        Pair(MalSymbol("apply"), MalFunction({ a: ISeq ->
            val function = a.nth(0) as MalFunction
            val params = MalList()
            a.seq().drop(1).forEach({ it ->
                if (it is ISeq) {
                    it.seq().forEach({ x -> params.conj_BANG(x) })
                } else {
                    params.conj_BANG(it)
                }
            })
            function.apply(params)
        })),

        Pair(MalSymbol("map"), MalFunction({ a: ISeq ->
            val function = a.nth(0) as MalFunction
            MalList((a.nth(1) as ISeq).seq().map({ it ->
                val params = MalList()
                params.conj_BANG(it)
                function.apply(params)
            }).toLinkedList())
        })),

        Pair(MalSymbol("nil?"), MalFunction({ a: ISeq -> if (a.nth(0) == NIL) TRUE else FALSE })),
        Pair(MalSymbol("true?"), MalFunction({ a: ISeq -> if (a.nth(0) == TRUE) TRUE else FALSE })),
        Pair(MalSymbol("false?"), MalFunction({ a: ISeq -> if (a.nth(0) == FALSE) TRUE else FALSE })),
        Pair(MalSymbol("symbol?"), MalFunction({ a: ISeq -> if (a.nth(0) is MalSymbol) TRUE else FALSE })),

        Pair(MalSymbol("symbol"), MalFunction({ a: ISeq -> MalSymbol((a.nth(0) as MalString).value) })),
        Pair(MalSymbol("keyword"), MalFunction({ a: ISeq ->
            val param = a.nth(0)
            if (param is MalKeyword) param else MalKeyword((a.nth(0) as MalString).value)
        })),
        Pair(MalSymbol("keyword?"), MalFunction({ a: ISeq -> if (a.nth(0) is MalKeyword) TRUE else FALSE })),
        Pair(MalSymbol("vector"), MalFunction({ a: ISeq -> MalVector(a) })),
        Pair(MalSymbol("vector?"), MalFunction({ a: ISeq -> if (a.nth(0) is MalVector) TRUE else FALSE })),

        Pair(MalSymbol("hash-map"), MalFunction({ a: ISeq ->
            val map = MalHashMap()
            pairwise(a).forEach({ it -> map.assoc_BANG(it.first as MalString, it.second) })
            map
        })),
        Pair(MalSymbol("map?"), MalFunction({ a: ISeq -> if (a.nth(0) is MalHashMap) TRUE else FALSE })),
        Pair(MalSymbol("assoc"), MalFunction({ a: ISeq ->
            val map = MalHashMap(a.first() as MalHashMap)
            pairwise(a.rest()).forEach({ it -> map.assoc_BANG(it.first as MalString, it.second) })
            map
        })),
        Pair(MalSymbol("dissoc"), MalFunction({ a: ISeq ->
            val map = MalHashMap(a.first() as MalHashMap)
            a.rest().seq().forEach({ it -> map.dissoc_BANG(it as MalString) })
            map
        })),
        Pair(MalSymbol("get"), MalFunction({ a: ISeq ->
            val map = a.nth(0) as? MalHashMap
            val key = a.nth(1) as MalString
            map?.elements?.get(key) ?: NIL
        })),
        Pair(MalSymbol("contains?"), MalFunction({ a: ISeq ->
            val map = a.nth(0) as? MalHashMap
            val key = a.nth(1) as MalString
            if (map?.elements?.get(key) != null) TRUE else FALSE
        })),
        Pair(MalSymbol("keys"), MalFunction({ a: ISeq ->
            val map = a.nth(0) as MalHashMap
            // Another situation where kotlinc breaks if I don't add this unnecessary cast
            MalList(map.elements.keys.map({ it -> it as MalType }).asSequence().toLinkedList())
        })),
        Pair(MalSymbol("vals"), MalFunction({ a: ISeq ->
            val map = a.nth(0) as MalHashMap
            MalList(map.elements.values.asSequence().toLinkedList())
        })),
        Pair(MalSymbol("count"), MalFunction({ a: ISeq ->
            val seq = a.nth(0) as? ISeq
            if (seq != null) MalInteger(seq.count().toLong()) else ZERO
        })),
        Pair(MalSymbol("sequential?"), MalFunction({ a: ISeq -> if (a.nth(0) is ISeq) TRUE else FALSE })),

        Pair(MalSymbol("with-meta"), MalFunction({ a: ISeq ->
            val obj = a.nth(0)
            val metadata = a.nth(1)
            obj.with_meta(metadata)
        })),
        Pair(MalSymbol("meta"), MalFunction({ a: ISeq -> a.first().metadata })),
        Pair(MalSymbol("conj"), MalFunction({ a: ISeq -> (a.first() as ISeq).conj(a.rest()) })),

        Pair(MalSymbol("atom"), MalFunction({ a: ISeq -> MalAtom(a.first()) })),
        Pair(MalSymbol("atom?"), MalFunction({ a: ISeq -> if (a.first() is MalAtom) TRUE else FALSE })),
        Pair(MalSymbol("deref"), MalFunction({ a: ISeq -> (a.first() as MalAtom).value })),
        Pair(MalSymbol("reset!"), MalFunction({ a: ISeq ->
            val atom = a.nth(0) as MalAtom
            val value = a.nth(1)
            atom.value = value
            value
        })),
        Pair(MalSymbol("swap!"), MalFunction({ a: ISeq ->
            val atom = a.nth(0) as MalAtom
            val function = a.nth(1) as MalFunction

            val params = MalList()
            params.conj_BANG(atom.value)
            a.seq().drop(2).forEach({ it -> params.conj_BANG(it) })

            val value = function.apply(params)
            atom.value = value

            value
        })),

        Pair(MalSymbol("readline"), MalFunction({ a: ISeq ->
            val prompt = a.first() as MalString
            try {
                MalString(readline(prompt.value))
            } catch (e: java.io.IOException) {
                throw MalException(e.message)
            } catch (e: EofException) {
                NIL
            }
        })),

        Pair(MalSymbol("time-ms"), MalFunction({ a: ISeq -> MalInteger(System.currentTimeMillis()) }))
)

fun pairwise(s: ISeq): List<Pair<MalType, MalType>> {
    val (keys, vals) = s.seq().withIndex().partition({ it -> it.index % 2 == 0 })
    return keys.map({ it -> it.value }).zip(vals.map({ it -> it.value }))
}

fun pairwiseCompare(s: ISeq, pred: (MalInteger, MalInteger) -> Boolean): MalConstant =
        if (pairwise(s).all({ it -> pred(it.first as MalInteger, it.second as MalInteger) })) TRUE else FALSE

fun pairwiseEquals(s: ISeq): MalConstant =
        if (pairwise(s).all({ it -> it.first == it.second })) TRUE else FALSE