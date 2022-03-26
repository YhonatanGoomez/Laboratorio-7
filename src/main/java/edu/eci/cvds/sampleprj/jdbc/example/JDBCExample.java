/*
 * Copyright (C) 2015 hcadavid
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.eci.cvds.sampleprj.jdbc.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class JDBCExample {

    public static void main(String args[]){
        try {
            String url="jdbc:mysql://desarrollo.is.escuelaing.edu.co:3306/bdprueba";
            String driver="com.mysql.jdbc.Driver";
            String user="bdprueba";
            String pwd="prueba2019";

            Class.forName(driver);
            Connection con=DriverManager.getConnection(url,user,pwd);
            con.setAutoCommit(false);


            System.out.println("Valor total pedido 1:"+valorTotalPedido(con, 1));

            List<String> prodsPedido=nombresProductosPedido(con, 1);


            System.out.println("Productos del pedido 1:");
            System.out.println("-----------------------");
            for (String nomprod:prodsPedido){
                System.out.println(nomprod);
            }
            System.out.println("-----------------------");


            int suCodigoECI=20134423;
            registrarNuevoProducto(con, suCodigoECI, "SU NOMBRE", 99999999);
            con.commit();


            con.close();

        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(JDBCExample.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    /**
     * Agregar un nuevo producto con los parámetros dados
     * @param con la conexión JDBC
     * @param codigo
     * @param nombre
     * @param precio
     * @throws SQLException
     */
    public static void registrarNuevoProducto(Connection con, int codigo, String nombre,int precio) throws SQLException{
        //Crear preparedStatement
        //Asignar parámetros
        //usar 'execute'
        String InsertString =
                "INSERT INTO ORD_PRODUCTOS VALUES (?, ?, ?)";
        try (PreparedStatement InsertProductos = con.prepareStatement(InsertString)){
            InsertProductos.setInt(1, codigo);
            InsertProductos.setString(2, nombre);
            InsertProductos.setInt(3, precio);
            InsertProductos.executeQuery();
            con.commit();
        }
        catch (SQLException e) {
            System.err.print("Transaction is being rolled back");
        }


    }

    /**
     * Consultar los nombres de los productos asociados a un pedido
     * @param con la conexión JDBC
     * @param codigoPedido el código del pedido
     * @return
     */
    public static List<String> nombresProductosPedido(Connection con, int codigoPedido) throws SQLException{
        //Crear prepared statement
        //asignar parámetros
        //usar executeQuery
        //Sacar resultados del ResultSet
        //Llenar la lista y retornarla
        String SelectString =
                "SELECT nombre FROM ORD_PRODUCTOS as op join ORD_DETALLE_PEDIDO odp on op.codigo = odp.producto_fk " +
                        "WHERE odp.pedido_fk = ?";
        List<String> np=new LinkedList<>();
        try (PreparedStatement selectProductos = con.prepareStatement(SelectString)){
                selectProductos.setInt(1, codigoPedido);
                ResultSet r =selectProductos.executeQuery();
                while (r.next()) {
                    np.add(r.getString("nombre"));
                }

                con.commit();
            }
        catch (SQLException e) {
                    System.err.print("Transaction is being rolled back");
        }

        return np;
    }


    /**
     * Calcular el costo total de un pedido
     * @param con
     * @param codigoPedido código del pedido cuyo total se calculará
     * @return el costo total del pedido (suma de: cantidades*precios)
     */
    public static int valorTotalPedido(Connection con, int codigoPedido){
        int valor = 0;
        //Crear prepared statement
        //asignar parámetros
        //usar executeQuery
        //Sacar resultado del ResultSet
        String SelectString =
                "SELECT SUM(d) as f from " +
                        "(SELECT precio*cantidad as d FROM ORD_PRODUCTOS as op join ORD_DETALLE_PEDIDO odp on op.codigo = odp.producto_fk " +
                        "WHERE odp.pedido_fk = ?) as total1";
        try (PreparedStatement selectPrecio = con.prepareStatement(SelectString)){
            selectPrecio.setInt(1, codigoPedido);
            ResultSet r =selectPrecio.executeQuery();
            while (r.next()){
                valor = r.getInt("f");
            }
        }
        catch (SQLException e) {
            System.err.print(e.getMessage());
        }
        return valor;
    }





}