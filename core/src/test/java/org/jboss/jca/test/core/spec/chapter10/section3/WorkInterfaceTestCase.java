/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jca.test.core.spec.chapter10.section3;

import org.jboss.jca.common.api.ThreadPool;
import org.jboss.jca.common.threadpool.ThreadPoolImpl;

import org.jboss.jca.test.core.spec.chapter10.SimpleWork;
import org.jboss.jca.test.core.spec.chapter10.common.SynchronizedWork;
import org.jboss.jca.test.core.spec.chapter10.common.UnsynchronizedWork;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * WorkInterfaceTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3.2
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class WorkInterfaceTestCase
{
   /*
    * Bootstrap (MC Facade)
    */
   private static EmbeddedTestMcBootstrap bootstrap;
   
   /**
    * Test for paragraph 2
    * The WorkManager dispatches a thread that calls the run method to
    *             begin execution of a Work instance.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testCallRunMethod() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      SimpleWork work = new SimpleWork();
      
      assertFalse(work.isCallRun());
      workManager.doWork(work);
      assertTrue(work.isCallRun());
   }

   /**
    * Test for paragraph 2
    * The WorkManager must catch any exception thrown during Work processing,
    *             which includes execution context setup, and wrap it with a 
    *             WorkCompletedException set to an appropriate error code, 
    *             which indicates the nature of the error condition.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testCatchAllExceptionAroundRun() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      SimpleWork work = new SimpleWork();
      work.setThrowWorkAException(true);
      
      try
      {
         workManager.doWork(work);
         fail("should throw WorkAException");
      } 
      catch (WorkException e)
      {
         assertNotNull(e);
         assertTrue(e instanceof WorkCompletedException);
         assertTrue(e.getMessage().indexOf("WorkAException") > 0);
      }
   }
   
   /**
    * Test for paragraph 3
    * The WorkManager may call the release method to request the active Work 
    *            instance to complete execution as soon as possible. 
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testReleaseMethod() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      ThreadPool tp = bootstrap.lookup("WorkManagerThreadPool", ThreadPoolImpl.class);

      SimpleWork work = new SimpleWork();
      work.setBlockRun(true);
      assertFalse(work.isCallRelease());
      
      workManager.startWork(work, WorkManager.IMMEDIATE, null, null);
      tp.stop(false);
      Thread.currentThread().sleep(3000);
      //assertTrue(work.isCallRelease());
      //TODO test fail here

   }
   
   /**
    * Test for paragraph 3
    * This would be called on a separate thread than the one currently executing the Work instance.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testCalledBySeparateThread() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 3
    * Since this method call causes the Work instance to be simultaneously acted upon
    *            by multiple threads, the Work instance implementation must be 
    *            thread-safe, and this method must be re-entrant.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testWorkInstanceThreadSafeAndReentrant() throws Throwable
   {
   }
   
   /**
    * Test for paragraph 5
    * Both the run and release methods in the Work implementation may contain synchronization 
    *            synchronization but they must not be declared as synchronized methods.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testCannotDeclaredSynchronizedSynchronizedWork() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      try
      {
         SynchronizedWork sw = new SynchronizedWork();
         workManager.doWork(sw);
         fail("Synchronized methods not catched");
      }
      catch (WorkException we)
      {
         // Expected
      }
   }
   
   /**
    * Test for paragraph 5
    * Both the run and release methods in the Work implementation may contain synchronization 
    *            synchronization but they must not be declared as synchronized methods.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testCannotDeclaredSynchronizedUnsynchronizedWork() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      UnsynchronizedWork usw = new UnsynchronizedWork();
      workManager.doWork(usw);
   }
   
   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception 
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set a new MC Bootstrap
      bootstrap = EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap();

      // Deploy Naming and Transaction
      bootstrap.deploy(WorkInterfaceTestCase.class.getClassLoader(), "naming-jboss-beans.xml");
      bootstrap.deploy(WorkInterfaceTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      
      // Deploy Beans
      bootstrap.deploy(WorkInterfaceTestCase.class);
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy Transaction and Naming
      bootstrap.undeploy(WorkInterfaceTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      bootstrap.undeploy(WorkInterfaceTestCase.class.getClassLoader(), "naming-jboss-beans.xml");

      // Undeploy Beans
      bootstrap.undeploy(WorkInterfaceTestCase.class);

      // Shutdown MC
      bootstrap.shutdown();

      // Set Bootstrap to null
      bootstrap = null;
   }
}

