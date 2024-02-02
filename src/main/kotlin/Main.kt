package org.example

import java.io.File


/**
 * 0. Продолжаем дорабатывать домашнее
 * задание из предыдущего семинара. За
 * основу берём код решения из
 * предыдущего домашнего задания.
 *
 * 1. Добавьте новую команду export, которая
 * экспортирует добавленные значения в
 * текстовый файл в формате JSON.
 * Команда принимает путь к новому файлу.
 * Например,
 * export /Users/user/myﬁle.json
 *
 * 2. Реализуйте DSL на Kotlin, который
 * позволит конструировать JSON и
 * преобразовывать его в строку.
 *
 * 3. Используйте этот DSL для экспорта
 * данных в файл.
 *
 * 4. Выходной JSON не обязательно должен
 * быть отформатирован, поля объектов
 * могут идти в любом порядке. Главное,
 * чтобы он имел корректный синтаксис.
 * Такой вывод тоже принимается:
 * [{"emails": ["ew@huh.ru"],"name":
 * "Alex","phones":
 * ["34355","847564"]}, {"emails": [],"name":
 * "Tom","phones": ["84755"]}]
 *
 * Записать текст в файл можно при помощи удобной функции-расширения writeText:
 * File("/Users/user/file.txt").writeText("Text to write")
 * Под капотом она использует такую конструкцию
 * FileOutputStream(file).use {it.write(text.toByteArray(Charsets.UTF_8))}
 *
 */

fun main() {
    val people: MutableList<Person> = mutableListOf()
    while (true) {
        when (val command = readCommand()) {

            is CheckAddCommand -> {
                if (command.isValid()) {
                    val person = people.find { it.name == command.name }
                    if (person == null) {
                        val newPerson = Person(command.name, mutableListOf(), mutableListOf())
                        if (command.type == "addPhone") {
                            newPerson.phones.add(command.value)
                        } else {
                            newPerson.emails.add(command.value)
                        }
                        people.add(newPerson)
                    } else {
                        if (command.type == "addPhone") {
                            person.phones.add(command.value)
                        } else {
                            person.emails.add(command.value)
                        }
                    }
                } else {
                    println("Неправильно введены данные.")
                    println(HelpCommand.info())
                }
            }

            is ShowCommand -> {
                val person = people.find { it.name == command.name }
                if (person != null) {
                    println("Последний добавленный контакт: $person")
                } else {
                    println("Контакт не найден.")
                }
            }

            is FindCommand -> {
                val result = people.filter {
                    if (command.type == "email") {
                        it.emails.contains(command.value)
                    } else {
                        it.phones.contains(command.value)
                    }
                }
                if (result.isNotEmpty()) {
                    println("Найденные контакты:")
                    result.forEach { println(it) }
                } else {
                    println("Контакты не найдены.")
                }
            }

            is ExportCommand -> {
                val file = File(command.path)
                if (file.exists()) {
                    val json = people.joinToString(separator = ",\n") { person ->
                                                """{
                            |   "name":"${person.name}",
                            |   "phones":[${person.phones.joinToString(",") { "\"$it\"" }}],
                            |   "emails":[${person.emails.joinToString(",") { "\"$it\"" }}]
                            |}""".trimMargin()
                    }
                    file.writeText("[$json]")
                    println("Данные экспортированы в файл ${command.path}.")
                } else {
                    println("Файл ${command.path} не найден.")
                }
            }

            HelpCommand -> println(HelpCommand.info())

            ExitCommand -> {
                println(ExitCommand.info())
                break
            }

            InputError -> {
                println(InputError.info())
                println(HelpCommand.info())
            }

        }
    }
}

sealed interface Command {
    fun isValid(): Boolean
}


data object InputError : Command {
    fun info(): String {
        return "Некорректная команда. Выводим help для получения справки."
    }

    override fun isValid(): Boolean = true
}


data class ShowCommand(val name: String) : Command {
    override fun isValid(): Boolean = true
}

data object HelpCommand : Command {
    fun info(): String {
        return "Список доступных команд:\n" +
                "addPhone <Имя> <Номер телефона> - добавить номер телефона для контакта\n" +
                "addEmail <Имя> <Адрес электронной почты> - добавить адрес электронной почты для контакта\n" +
                "show <Имя> - вывести последний добавленный контакт\n" +
                "findEmail <Адрес электронной почты> - найти контакты по адресу электронной почты\n" +
                "findPhone <Номер телефона> - найти контакты по номеру телефона\n" +
                "export /home/ol/projects/JavaProjects/AndroidGB/Kotlin/KotlinHomeWork4/src/main/test.json\n" +
                "help - вывести справку\n" +
                "exit - выход из программы\n"
    }

    override fun isValid(): Boolean {
        return true
    }
}

data object ExitCommand : Command {
    fun info(): String {
        return "Выход из программы\n"
    }

    override fun isValid(): Boolean = true
}

data class FindCommand(val type: String, val value: String) : Command {
    override fun isValid(): Boolean {
        return true
    }
}

data class Person(val name: String, val phones: MutableList<String>, val emails: MutableList<String>) {
    override fun toString(): String {
        return "Имя: $name\nТелефоны:$phones\nEmail: $emails"
    }
}

data class ExportCommand(val path: String) : Command {
    override fun isValid(): Boolean {
        return true
    }
}

fun readCommand(): Command {
    val type: String
    val name: String
    val value: String

    print("Введите команду: ")
    val command = readlnOrNull()?.split(" ")
    type = command!![0]

    return when (type) {
        "addPhone" -> {
            name = command[1]
            value = command[2]
            CheckAddCommand(type, name, value)
        }

        "addEmail" -> {
            name = command[1]
            value = command[2]
            CheckAddCommand(type, name, value)
        }

        "show" -> {
            name = command[1]
            ShowCommand(name)
        }

        "findEmail" -> {
            value = command[1]
            FindCommand("email", value)
        }

        "findPhone" -> {
            value = command[1]
            FindCommand("phone", value)
        }

        "export" -> {
            val path = command[1]
            ExportCommand(path)
        }

        "help" -> HelpCommand
        "exit" -> ExitCommand

        else -> {
            InputError
        }
    }
}

class CheckAddCommand(val type: String, val name: String, val value: String) : Command {
    override fun isValid(): Boolean {
        val phonePattern = """\+\d{12}$"""
        val emailPattern = """[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"""
        val namePattern = """[a-zA-Z]{2,}$"""
        if (type == "addPhone") {
            return when {
                name.matches(Regex(namePattern)) && value.matches(Regex(phonePattern)) -> {
                    true
                }

                else -> false
            }
        } else {
            return when {
                name.matches(Regex(namePattern)) && value.matches(Regex(emailPattern)) -> {
                    true
                }

                else -> false
            }
        }
    }
}
