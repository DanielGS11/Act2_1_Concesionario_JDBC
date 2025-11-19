************************ GESTOR DE BASE DE DATOS MYSQL Y SQLITE CREADO POR DANIEL GARCÍA SALAS *************************

FINALIDAD: Realizar una gestion de la base de datos ya sea en MySQL o SQLite

NO MODIFICAR NINGUN ARCHIVO, PARA EMPEZAR A USAR EL GESTOR UTILICE EL ARCHIVO 'Main' UBICADO EN '/src/main/java'

ESTA VERSION DEL GESTOR ES UNA VERSION PRIMITIVA, POR LO QUE PUEDE NO SER TAN COMPLEJO NI COMPLETO COMO OTROS GESTORES

VENTAJAS E INCONVENIENTES ENTRE MySQL Y SQLite
- SQLite:
    -- VENTAJAS:
        --- No necesita un servidor ya que el propio ordenador tiene la base de datos
        --- Al ser el propio ordenador el 'servidor' de la base de datos, el proceso es mas rapido
        --- No necesita una configuracion de usuario y puertos, unicamente la ruta de la base de datos
        --- No necesita que la base de datos se cree, ya que puede crearla en la ruta designada

    -- INCONVENIENTES:
        --- Necesita que se activen manualmente las claves foraneas (Foreign Keys)
            con el comando 'PRAGMA foreign_keys = ON'
        --- El limite de usuarios que pueden acceder a la vez a la Base de Datos es 1
        --- No tiene soporte para Procedures

- MySQL:
    -- VENTAJAS:
        --- No necesita activar las claves foraneas (Foreign Keys) manualmente
        --- Soporta Procedures, con los que se almacena en una especie de Script varias sentencias SQL
        --- Pueden acceder multiples usuarios a la vez

    -- INCONVENIENTES:
        --- Mas lento al ser un servidor SQL
        --- Necesita configurar ruta, usuario, puerto y contraseña correctas para acceder a la base de datos