package com.winterhaven_mc.deathchest;

import static org.junit.Assert.*;

import org.junit.Test;

import static org.mockito.Mockito.*;

import org.bukkit.Material;
import org.bukkit.block.Block;

import static com.winterhaven_mc.deathchest.DeathChestBlock.*;


public class DeathChestBlockTest {

	Block mockBlock = mock(Block.class);

	@Test
	public void testIsDeathSign1() {
		
		// test when block type is not a wall sign or sign post
		when(mockBlock.getType()).thenReturn(Material.DIRT);
		assertFalse("Dirt block is not a DeathSign.",isDeathSign(mockBlock));

	}

	@Test
	public void testIsDeathSign2() {
		
		// test when block type is a wall sign without deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Wall sign block without metadata is not a DeathSign.",isDeathSign(mockBlock));
	}
		
	@Test
	public void testIsDeathSign3() {

		// test when block type is a wall sign with deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertTrue("Wall sign with deathchest-owner metadata is a DeathSign.",isDeathSign(mockBlock));
	}
	
	@Test
	public void testIsDeathSign4() {
		
		// test when block type is a sign post without deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.SIGN_POST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Sign post block without metadata is not a DeathSign.",isDeathSign(mockBlock));
	}
	
	@Test
	public void testIsDeathSign5() {
		
		// test when block type is a sign post with deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.SIGN_POST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertTrue("Sign post with deathchest-owner metadata is a DeathSign.",isDeathSign(mockBlock));
	}
	
	@Test
	public void testIsDeathSign6() {
		
		// test when block type is a chest without deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.CHEST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Chest block with deathchest-owner metadata is not a DeathSign.",isDeathSign(mockBlock));
	}
	
	@Test
	public void testIsDeathSign7() {
		
		// test when block type is a chest with deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.CHEST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertFalse("Chest block with deathchest-owner metadata is not a DeathSign.",isDeathSign(mockBlock));
	}
	
	@Test
	public void testIsDeathSign8() {
		
		// test when passed null
		assertFalse("Return false when passed null.",isDeathSign(null));
	}


	@Test
	public void testIsDeathChest1() {

		// test when block type is not a chest or sign
		when(mockBlock.getType()).thenReturn(Material.DIRT);
		assertFalse("Dirt block is not a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest2() {

		// test when block type is a wall sign without deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Wall sign block without metadata is not a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest3() {

		// test when block type is a wall sign with deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertFalse("Wall sign with deathchest-owner metadata is not a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest4() {

		// test when block type is a sign post without deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.SIGN_POST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Sign post block without metadata is not a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest5() {

		// test when block type is a sign post with deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.SIGN_POST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertFalse("Sign post with deathchest-owner metadata is not a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest6() {

		// test when block type is a chest without deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.CHEST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Chest block without deathchest-owner metadata is not a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest7() {

		// test when block type is a chest with deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.CHEST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertTrue("Chest block with deathchest-owner metadata is a DeathChest.",isDeathChest(mockBlock));
	}

	@Test
	public void testIsDeathChest8() {

		// test when passed null
		assertFalse("Return false when passed null.",isDeathChest(null));
	}

	
	@Test
	public void testIsDeathChestBlock1() {
		
		// test when block type is not a chest or sign
		when(mockBlock.getType()).thenReturn(Material.DIRT);
		assertFalse("Dirt block is not a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock2() {
		
		// test when block type is a wall sign without deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Wall sign block without metadata is not a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock3() {
		
		// test when block type is a wall sign with deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.WALL_SIGN);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertTrue("Wall sign with deathchest-owner metadata is a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock4() {
		
		// test when block type is a sign post without deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.SIGN_POST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Sign post block without metadata is not a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock5() {
		
		// test when block type is a sign post with deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.SIGN_POST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertTrue("Sign post with deathchest-owner metadata is a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock6() {
		
		// test when block type is a chest without deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.CHEST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(false);
		assertFalse("Chest block without deathchest-owner metadata is not a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock7() {
		
		// test when block type is a chest with deathchest metadata
		when(mockBlock.getType()).thenReturn(Material.CHEST);
		when(mockBlock.hasMetadata("deathchest-owner")).thenReturn(true);
		assertTrue("Chest block with deathchest-owner metadata is a DeathChestBlock.",isDeathChestBlock(mockBlock));
	}
	
	@Test
	public void testIsDeathChestBlock8() {
		
		// test when passed null
		assertFalse("Return false when passed null.",isDeathChestBlock(null));	
	}

}
